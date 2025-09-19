package com.hexaoracle.server.application.hexagram;

import com.hexaoracle.server.application.hexagram.dto.HexagramDetailDto;
import com.hexaoracle.server.domain.hexagram.port.HexagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetHexagramUseCase {

    private final HexagramRepository hexagramRepository;

    public HexagramDetailDto execute(short no, String locale) {
        return hexagramRepository.findByNo(no, locale)
                .orElseThrow(() -> new IllegalArgumentException("Hexagram not found: " + no));
    }
}
