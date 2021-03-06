package org.diagnoseit.rules.result;

import java.util.ArrayList;
import java.util.List;

import org.diagnoseit.engine.session.ISessionResultCollector;
import org.diagnoseit.engine.session.SessionContext;
import org.diagnoseit.engine.tag.Tag;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.rules.result.ProblemOccurrence.CauseStructure;
import org.diagnoseit.rules.util.AggregatedCallable;
import org.spec.research.open.xtrace.api.core.callables.Callable;



/**
 * @author Alexander Wert
 *
 */
public class ProblemInstanceResultCollector<I> implements
		ISessionResultCollector<I, List<ProblemOccurrence>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProblemOccurrence> collect(SessionContext<I> sessionContext) {
		List<ProblemOccurrence> problems = new ArrayList<>();
		//Trace inputTrace = sessionContext.getInput();
		//Collection<Tag> leafTags = sessionContext.getStorage()
		//		.mapTags(TagState.LEAF).values();
		//for (Tag leafTag : leafTags) {
			/*ProblemOccurrence problem = new ProblemOccurrence(inputTrace,
					getGlobalContext(leafTag), getProblemContext(leafTag),
					getRootCauseInvocations(leafTag),
					getCauseStructure(leafTag));
			*/
			//getSubTrace(leafTag);
			//problems.add(problem);
		//}
		return problems;
	}

	private Callable getGlobalContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_GLOBAL_CONTEXT)) {
				return (Callable) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Global context could not be found!");
	}

	private Callable getProblemContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CONTEXT)) {
				return (Callable) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem context could not be found!");
	}

	private AggregatedCallable getRootCauseInvocations(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CAUSE)) {
				return (AggregatedCallable) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem root cause could not be found!");
	}

	private CauseStructure getCauseStructure(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_CAUSE_STRUCTURE)) {
				return (CauseStructure) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Cause structure could not be found!");
	}

	private Object getTagByRuleConstants(Tag leafTag, String ruleConstants,
			String errorMessage) {
		while (null != leafTag) {
			if (leafTag.getType().equals(ruleConstants)) {
				return leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}
		throw new RuntimeException(errorMessage);
	}

//	private SubTrace getSubTrace(Tag leafTag) {
//		return (SubTrace) getTagByRuleConstants(leafTag,
//				RuleConstants.TEST_STRUCTURE,
//				"Subtrace could not be found!");
//	}
}
