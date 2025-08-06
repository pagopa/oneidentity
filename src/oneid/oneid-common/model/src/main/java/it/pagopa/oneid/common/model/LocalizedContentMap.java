package it.pagopa.oneid.common.model;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class LocalizedContentMap {

  @Valid
  @Builder.Default
  private Map<String, Map<String, LocalizedContent>> contentMap = new HashMap<>() {{
    List.of("IT", "FR", "DE", "SL", "EN").forEach(lang -> put(lang, new HashMap<>()));
  }};


}
