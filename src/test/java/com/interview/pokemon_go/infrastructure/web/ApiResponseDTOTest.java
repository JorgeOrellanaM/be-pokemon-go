package com.interview.pokemon_go.infrastructure.web;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ApiResponseDTOTest {

    private static final ErrorResponseDTO ERROR = new ErrorResponseDTO(400, "Bad Request", "bad",
            "/api/v1/pokemon", Instant.now(), "6f1c2a9e", List.of());

    @Test
    void okCarriesDataAndNoError() {
        ApiResponseDTO<String> response = ApiResponseDTO.ok("pikachu");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("pikachu");
        assertThat(response.error()).isNull();
    }

    @Test
    void okRequiresData() {
        assertThatNullPointerException().isThrownBy(() -> ApiResponseDTO.ok(null));
    }

    @Test
    void failureCarriesErrorAndNoData() {
        ApiResponseDTO<Void> response = ApiResponseDTO.failure(ERROR);

        assertThat(response.success()).isFalse();
        assertThat(response.error()).isEqualTo(ERROR);
        assertThat(response.data()).isNull();
    }

    @Test
    void failureRequiresError() {
        assertThatNullPointerException().isThrownBy(() -> ApiResponseDTO.failure(null));
    }

    @Test
    void rejectsSuccessWithError() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ApiResponseDTO<>(true, null, ERROR));
    }

    @Test
    void rejectsFailureWithoutError() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ApiResponseDTO<>(false, null, null));
    }

    @Test
    void rejectsDataTogetherWithError() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ApiResponseDTO<>(false, "data", ERROR));
    }
}
