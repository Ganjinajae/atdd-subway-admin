package nextstep.subway.acceptance.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import nextstep.subway.acceptance.base.BaseAcceptanceTest;
import nextstep.subway.acceptance.station.StationRestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("지하철노선 관련 기능")
class LineAcceptanceTest extends BaseAcceptanceTest {
    long upStationId;
    long downStationId;
    @BeforeEach
    protected void setUp() {
        super.setUp();
        // Given
        upStationId = StationRestAssured.지하철역_생성_요청("지하철역").jsonPath().getLong("id");
        downStationId = StationRestAssured.지하철역_생성_요청("새로운지하철역").jsonPath().getLong("id");
    }

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철노선 생성")
    @Test
    void createLine() {
        // when
        LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10);

        // then
        ExtractableResponse<Response> showResponse = LineRestAssured.지하철노선_목록_조회_요청();
        List<String> lineNames = showResponse.jsonPath().getList("name", String.class);
        List<String> colors = showResponse.jsonPath().getList("color", String.class);
        List<String> stations = showResponse.jsonPath().getList("stations[0].name", String.class);
        assertAll(
                () -> assertThat(lineNames).containsAnyOf("신분당선"),
                () -> assertThat(colors).containsAnyOf("bg-red-600"),
                () -> assertThat(stations).containsOnly("지하철역","새로운지하철역")
        );
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성하면
     * Then 지하철 노선이 생성이 안된다
     */
    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성하면 노선이 생성이 안된다.")
    @Test
    void createLineWithDuplicateName() {
        // Given
        LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10);

        // when
        ExtractableResponse<Response> response = LineRestAssured.지하철노선_생성_요청("신분당선", "bg-blue-600", upStationId, downStationId, 10);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철노선 목록 조회")
    @Test
    void showLines() {
        // Given
        LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10);
        LineRestAssured.지하철노선_생성_요청("5호선", "bg-blue-600", upStationId, downStationId, 10);

        // when
        ExtractableResponse<Response> showResponse = LineRestAssured.지하철노선_목록_조회_요청();

        // then
        List<String> lineNames = showResponse.jsonPath().getList("name", String.class);
        List<String> colors = showResponse.jsonPath().getList("color", String.class);
        List<String> stations1 = showResponse.jsonPath().getList("stations[0].name", String.class);
        List<String> stations2 = showResponse.jsonPath().getList("stations[1].name", String.class);
        assertAll(
                () -> assertThat(lineNames).containsExactly("신분당선", "5호선"),
                () -> assertThat(lineNames).hasSize(2),
                () -> assertThat(colors).containsExactly("bg-red-600", "bg-blue-600"),
                () -> assertThat(stations1).containsOnly("지하철역", "새로운지하철역"),
                () -> assertThat(stations2).containsOnly("지하철역", "새로운지하철역")
        );
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철노선 조회")
    @Test
    void showLine() {
        // Given
        long id = LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10).jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> showResponse = LineRestAssured.지하철노선_조회_요청(id);

        // then
        String lineNames = showResponse.jsonPath().getString("name");
        String colors = showResponse.jsonPath().getString("color");
        List<String> stations = showResponse.jsonPath().getList("stations.name");
        assertAll(
                () -> assertThat(lineNames).isEqualTo("신분당선"),
                () -> assertThat(colors).isEqualTo("bg-red-600"),
                () -> assertThat(stations).containsOnly("지하철역", "새로운지하철역")
        );
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철노선 수정")
    @Test
    void updateLine() {
        // Given
        long id = LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10).jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> updateResponse = LineRestAssured.지하철노선_변경_요청(id, "신분당선", "bg-blue-600");

        // then
        assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @DisplayName("지하철노선 삭제")
    @Test
    void deleteLine() {
        // Given
        ExtractableResponse<Response> createResponse = LineRestAssured.지하철노선_생성_요청("신분당선", "bg-red-600", upStationId, downStationId, 10);
        long id = createResponse.jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> deleteResponse = LineRestAssured.지하철노선_제거_요청(id);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}