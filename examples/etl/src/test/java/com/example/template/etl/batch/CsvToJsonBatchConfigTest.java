package com.example.template.etl.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.etl.batch.CsvToJsonBatchConfig.InputRecord;
import com.example.template.etl.batch.CsvToJsonBatchConfig.OutputRecord;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;

/**
 * Unit tests for {@link CsvToJsonBatchConfig} beans, instantiated directly without a Spring context
 * or a full batch run.
 */
class CsvToJsonBatchConfigTest {

  private final CsvToJsonBatchConfig config = new CsvToJsonBatchConfig();

  @Test
  void transformProcessor_assignsStandardTierBelowOneThousand() throws Exception {
    OutputRecord out =
        config.transformProcessor().process(new InputRecord("1", "Pen", "999.99", "OFFICE"));

    assertThat(out).isNotNull();
    assertThat(out.tier()).isEqualTo("STANDARD");
    assertThat(out.amountUsd()).isEqualTo(999.99);
    assertThat(out.id()).isEqualTo("1");
    assertThat(out.name()).isEqualTo("Pen");
    assertThat(out.category()).isEqualTo("OFFICE");
  }

  @Test
  void transformProcessor_assignsBusinessTierFromOneThousand() throws Exception {
    ItemProcessor<InputRecord, OutputRecord> processor = config.transformProcessor();

    OutputRecord lowerBound = processor.process(new InputRecord("2", "Desk", "1000", "FURNITURE"));
    OutputRecord upperBound =
        processor.process(new InputRecord("3", "Chair", "9999.99", "FURNITURE"));

    assertThat(lowerBound).isNotNull();
    assertThat(lowerBound.tier()).isEqualTo("BUSINESS");
    assertThat(upperBound).isNotNull();
    assertThat(upperBound.tier()).isEqualTo("BUSINESS");
  }

  @Test
  void transformProcessor_assignsEnterpriseTierFromTenThousand() throws Exception {
    OutputRecord out =
        config
            .transformProcessor()
            .process(new InputRecord("7", "System G", "10000", "ELECTRONICS"));

    assertThat(out).isNotNull();
    assertThat(out.tier()).isEqualTo("ENTERPRISE");
    assertThat(out.amountUsd()).isEqualTo(10_000.0);
  }

  @Test
  void transformProcessor_filtersOutNonPositiveAmountsByReturningNull() throws Exception {
    ItemProcessor<InputRecord, OutputRecord> processor = config.transformProcessor();

    assertThat(processor.process(new InputRecord("4", "Refund", "-50.00", "SERVICES"))).isNull();
    assertThat(processor.process(new InputRecord("5", "Freebie", "0", "SERVICES"))).isNull();
  }

  @Test
  void transformProcessor_throwsNumberFormatExceptionForMalformedAmount() {
    ItemProcessor<InputRecord, OutputRecord> processor = config.transformProcessor();

    // The step's fault tolerance (skipLimit 10) handles this during a real run.
    assertThatThrownBy(() -> processor.process(new InputRecord("8", "Supply", "invalid", "MISC")))
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  void csvReader_readsAllRowsFromClasspathCsvSkippingHeader() throws Exception {
    FlatFileItemReader<InputRecord> reader = config.csvReader();
    List<InputRecord> rows = new ArrayList<>();

    reader.open(new ExecutionContext());
    try {
      for (InputRecord row = reader.read(); row != null; row = reader.read()) {
        rows.add(row);
      }
    } finally {
      reader.close();
    }

    assertThat(rows).hasSize(10);
    assertThat(rows.getFirst())
        .isEqualTo(new InputRecord("1", "Widget A", "1500.00", "ELECTRONICS"));
    assertThat(rows.getLast()).isEqualTo(new InputRecord("10", "Kit J", "990.00", "INDUSTRIAL"));
    // The header line is skipped, not mapped as data.
    assertThat(rows).extracting(InputRecord::id).doesNotContain("id");
  }
}
