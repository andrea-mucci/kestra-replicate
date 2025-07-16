package io.clariteia.plugin.replicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Run a Replicate Prediction",
    description = "Run a prediction using Replicate API"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "replicate prediction",
            code = { "format: \"Text to be reverted\"" }
        )
    }
)
public class ReplicatePredictionTask extends Task implements RunnableTask<ReplicatePredictionTask.Output> {
    @Schema(
        title = "the replicate model name",
        description = "the replicate model name like \"black-forest-labs/flux-kontext-dev\" "
    )
    private Property<@NotNull @NotBlank @NotEmpty String> model_name;

    @Schema(
        title = "the model inputs",
        description = "all the models require a set of specific inputs"
    )
    private Property<@NotNull @NotBlank @NotEmpty Map<String, Object>> inputs;

    @Schema(
        title = "Replicate API token",
        description = "the API token provided by Replicate"
    )
    private Property<@NotNull @NotBlank @NotEmpty String> token;

    @Override
    public ReplicatePredictionTask.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String render_model_name = runContext.render(model_name).as(String.class).orElse("");
        logger.debug(render_model_name);

        String render_token = runContext.render(token).as(String.class).orElse("");
        logger.debug(render_token);

        Map<String, Object> inputMap = runContext.render(inputs).as(Map.class).orElse("");
        logger.debug(render_token);

        HttpRequest versionRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.replicate.com/v1/models/" + model + "/versions"))
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", "Token " + token)
            .header("Content-Type", "application/json")
            .GET()
            .build();


        return Output.builder()
            .child(new OutputChild(StringUtils.reverse(render_model_name)))
            .build();
    }

    /**
     * Input or Output can be nested as you need
     */
    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Short description for this output",
            description = "Full description of this output"
        )
        private final OutputChild child;
    }

    @Builder
    @Getter
    public static class OutputChild implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Short description for this output",
            description = "Full description of this output"
        )
        private final String value;
    }
}
