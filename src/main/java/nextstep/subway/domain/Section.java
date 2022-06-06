package nextstep.subway.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.dao.DataIntegrityViolationException;

@Entity
public class Section extends BaseEntity {
    public static final String ERROR_DISTANCE_OVER = "중간 구간의 길이가 상위 구간의 길이보다 길거나 같습니다.";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "line_id")
    private Long lineId;
    private Long upStationId;
    private Long downStationId;
    private Integer distance;
    private Integer order;

    protected Section() {
    }

    public Section(Long lineId, Long upStationId, Long downStationId, Integer distance) {
        this.lineId = lineId;
        this.upStationId = upStationId;
        this.downStationId = downStationId;
        this.distance = distance;
    }

    public boolean match(Section section) {
        if (Objects.equals(this.upStationId, section.upStationId)
                || Objects.equals(this.downStationId, section.downStationId)) {
            validateDistanceOver(section);
            return true;
        }

        return false;
    }

    private void validateDistanceOver(Section section) {
        if (distance <= section.distance) {
            throw new DataIntegrityViolationException(ERROR_DISTANCE_OVER);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getLineId() {
        return lineId;
    }

    public Long getUpStationId() {
        return upStationId;
    }

    public Long getDownStationId() {
        return downStationId;
    }

    public Integer getDistance() {
        return distance;
    }

    public Integer getOrder() {
        return order;
    }

    public void updateUpStationId(Long upStationId) {
        this.upStationId = upStationId;
    }

    public void updateDownStationId(Long downStationId) {
        this.downStationId = downStationId;
    }

    public void updateDistance(Integer distance) {
        this.distance = distance;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }

    public void increaseOrder() {
        this.order++;
    }
}
