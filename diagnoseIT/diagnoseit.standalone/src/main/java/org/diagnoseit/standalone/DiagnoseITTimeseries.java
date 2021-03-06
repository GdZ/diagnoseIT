package org.diagnoseit.standalone;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.diagnoseit.engine.DiagnosisEngine;
import org.diagnoseit.engine.DiagnosisEngineConfiguration;
import org.diagnoseit.engine.IDiagnosisEngine;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemInstanceResultCollector;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.diagnoseit.rules.timeseries.impl.InfluxDBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class DiagnoseITTimeseries implements Runnable {
	private static final long TIMEOUT = 50;

	/** The logger of this class. */
	private static final Logger log = LoggerFactory.getLogger(DiagnoseIT.class);

	private final int capacity = 100;

	// influx
	private final BlockingQueue<DiagnosisInput> queue = new LinkedBlockingQueue<>(
			capacity);

	private final ExecutorService executor = Executors
			.newSingleThreadExecutor();

	private final List<String> rulesPackages;

	private IDiagnosisEngine<InfluxDBConnector> engine;

	public DiagnoseITTimeseries(List<String> rulesPackages) {
		this.rulesPackages = rulesPackages;
	}

	// influx
	public boolean diagnose(InfluxDBConnector connector) {
		try {
			// influx
			return queue.offer(new DiagnosisInput(connector), TIMEOUT,
					TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

//	public int diagnose(Collection<Pair<Trace, Long>> traceBaselinePairs) {
//		int count = 0;
//		for (Pair<Trace, Long> invocationBaselinePair : traceBaselinePairs) {
//			boolean successfullySubmitted = diagnose(
//					invocationBaselinePair.getLeft(),
//					invocationBaselinePair.getRight());
//			if (!successfullySubmitted) {
//				break;
//			}
//			count++;
//		}
//		return count;
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			while (true) {
				DiagnosisInput diagnosisInput = queue.take();
				engine.analyze(diagnosisInput.getConnector());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void init(ISessionCallback<List<ProblemOccurrence>> resultHandler)
			throws ClassNotFoundException {

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Rule.class));
		Set<Class<?>> ruleClasses = new HashSet<>();
		for (String packageName : rulesPackages) {
			for (BeanDefinition bd : scanner
					.findCandidateComponents(packageName)) {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				ruleClasses.add(clazz);
			}
		}

		DiagnosisEngineConfiguration<InfluxDBConnector, List<ProblemOccurrence>> configuration = new DiagnosisEngineConfiguration<InfluxDBConnector, List<ProblemOccurrence>>();

		configuration.setNumSessionWorkers(2);
		configuration.setRuleClasses(ruleClasses);
		configuration.setResultCollector(new ProblemInstanceResultCollector<InfluxDBConnector>());
		configuration.setSessionCallback(resultHandler);

		engine = new DiagnosisEngine<>(configuration);
		executor.execute(this);
		if (log.isInfoEnabled()) {
			log.info("|-Diagnosis Service active...");
		}
	}

	private static class DiagnosisInput {
		private final InfluxDBConnector connector;

		/**
		 * @param invocation
		 * @param baseline
		 */
		public DiagnosisInput(InfluxDBConnector connector) {
			this.connector = connector;
		}
		/**
		 * 
		 * @return
		 */
		public InfluxDBConnector getConnector() {
			return connector;
		}

	}
}
