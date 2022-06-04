package nextstep.subway.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Sections;
import nextstep.subway.domain.Station;
import nextstep.subway.dto.LineRequest;
import nextstep.subway.dto.LineResponse;
import nextstep.subway.dto.LineUpdateRequest;
import nextstep.subway.dto.SectionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineService {
    private final LineRepository lineRepository;
    private final StationService stationService;
    private final SectionService sectionService;

    public LineService(LineRepository lineRepository, StationService stationService, SectionService sectionService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
        this.sectionService = sectionService;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Section section = sectionService.generateSection(lineRequest);
        sectionService.saveSection(section);

        Line persistLine = lineRepository.save(lineRequest.toLine());
        persistLine.addSection(section);

        return LineResponse.of(persistLine);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAllLines() {
        List<Line> lines = lineRepository.findAll();

        return lines.stream()
                .map(LineResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse findLineById(Long id) {
        return lineRepository.findById(id)
                .map(LineResponse::of)
                .orElseThrow(() -> new EntityNotFoundException("지하철노선이 없습니다."));
    }

    @Transactional
    public void updateLine(Long id, LineUpdateRequest lineUpdateRequest) {
        Line line = getLine(id);
        line.update(lineUpdateRequest.toLine());
    }

    @Transactional
    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    @Transactional
    public Line addSection(Long id, SectionRequest sectionRequest) {
        Line line = getLine(id);
        Station upStation = getStation(sectionRequest.getUpStationId());
        Station downStation = getStation(sectionRequest.getDownStationId());
        Section newSection = sectionRequest.createSection(upStation, downStation);

        validateAdd(line, newSection);

        line.addSection(newSection);
        return line;
    }

    public void removeSectionByStationId(Long lineId, Long stationId) {
        Line line = getLine(lineId);
        Station station = getStation(stationId);

        validateRemove(line, station);

        line.removeSectionByStation(station);
    }

    private Line getLine(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("지하철노선이 없습니다."));
    }

    private Station getStation(Long id) {
        return stationService.findStationById(id);
    }

    private void validateAdd(Line line, Section newSection) {
        Set<Station> stations = line.getSections().allStations();
        if (stations.containsAll(newSection.allStations())) {
            throw new IllegalArgumentException("상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가할 수 없습니다.");
        }
        if (!stations.contains(newSection.getUpStation()) && !stations.contains(newSection.getDownStation())) {
            throw new IllegalArgumentException("상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가할 수 없습니다.");
        }
    }

    private void validateRemove(Line line, Station station) {
        Sections sections = line.getSections();
        if (sections.size() == Sections.MIN_SECTION_COUNT) {
            throw new IllegalStateException("단일 구간인 노선은 구간을 제거할 수 없습니다.");
        }
        if (!sections.allStations().contains(station)) {
            throw new IllegalArgumentException("노선에 등록되지 않은 구간은 제거할 수 없습니다.");
        }
    }
}
