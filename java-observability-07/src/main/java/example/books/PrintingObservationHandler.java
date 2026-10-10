package example.books;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Observationの開始と終了を受け取るハンドラー。名前が demo. で始まるものだけを扱う
@Component
public class PrintingObservationHandler implements ObservationHandler<Observation.Context> {
    private static final Logger log = LoggerFactory.getLogger(PrintingObservationHandler.class);

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context.getName() != null && context.getName().startsWith("demo.");
    }

    @Override
    public void onStart(Observation.Context context) {
        log.info("[開始] name={}", context.getName());
    }

    @Override
    public void onScopeOpened(Observation.Context context) {
        log.info("[スコープを開いた] name={}", context.getName());
    }

    @Override
    public void onStop(Observation.Context context) {
        log.info("[終了] name={} low={} high={}", context.getName(),
                context.getLowCardinalityKeyValues().stream().map(KeyValue::toString).toList(),
                context.getHighCardinalityKeyValues().stream().map(KeyValue::toString).toList());
    }
}
