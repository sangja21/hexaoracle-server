// domain/hexagram/port/HexagramRepository.java
package com.hexaoracle.server.domain.hexagram.port;

import com.hexaoracle.server.application.hexagram.dto.HexagramDto;
import java.util.List;


public interface HexagramRepository {
    List<HexagramDto> findAll();
}
