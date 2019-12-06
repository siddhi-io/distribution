package io.siddhi.langserver.completion.providers.definitionaggregation;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Provide Completions for Aggregation time Context
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Aggregation_timeContext}.
 */
public class AggregationTimeContextProvider extends CompletionProvider {

    public AggregationTimeContextProvider() {
        this.providerName = SiddhiQLParser.Aggregation_timeContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<CompletionItem> completions;
        completions =
                LSCompletionContext.INSTANCE.getProvider(SiddhiQLParser.Aggregation_time_rangeContext.class.getName())
                        .getCompletions();
        return completions;
    }
}
