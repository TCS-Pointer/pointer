package br.com.pointer.pointer_back.mapper;

import org.springframework.stereotype.Component;

import br.com.pointer.pointer_back.dto.ComunicadoDTO;
import br.com.pointer.pointer_back.model.Comunicado;

@Component
public class ComunicadoMapper {

    public ComunicadoDTO toDTO(Comunicado comunicado) {
        if (comunicado == null) {
            return null;
        }

        ComunicadoDTO dto = new ComunicadoDTO();
        dto.setId(comunicado.getId());
        dto.setTitulo(comunicado.getTitulo());
        dto.setDescricao(comunicado.getDescricao());
        dto.setSetor(comunicado.getSetor());
        dto.setCargo(comunicado.getCargo());
        dto.setDataPublicacao(comunicado.getDataPublicacao());

        return dto;
    }

    public Comunicado toEntity(ComunicadoDTO dto) {
        if (dto == null) {
            return null;
        }

        Comunicado comunicado = new Comunicado();
        comunicado.setId(dto.getId());
        comunicado.setTitulo(dto.getTitulo());
        comunicado.setDescricao(dto.getDescricao());
        comunicado.setSetor(dto.getSetor());
        comunicado.setCargo(dto.getCargo());
        // Não setamos a dataPublicacao aqui pois ela é gerenciada pelo @PrePersist

        return comunicado;
    }
}
