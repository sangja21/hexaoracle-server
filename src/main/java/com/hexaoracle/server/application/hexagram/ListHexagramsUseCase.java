// application/hexagram/ListHexagramsUseCase.java
package com.hexaoracle.server.application.hexagram;

import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import com.hexaoracle.server.domain.hexagram.port.HexagramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ListHexagramsUseCase {

    private final HexagramRepository hexagramRepository;

    public List<HexagramDto> execute() {
        return hexagramRepository.findAll();
    }
}
