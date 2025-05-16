package br.com.pointer.pointer_back.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.pointer.pointer_back.dto.ComunicadoDTO;
import br.com.pointer.pointer_back.mapper.ComunicadoMapper;
import br.com.pointer.pointer_back.model.Comunicado;
import br.com.pointer.pointer_back.repository.ComunicadoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ComunicadoService {

    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @Autowired
    private ComunicadoMapper comunicadoMapper;

    public List<ComunicadoDTO> listarTodos() {
        return comunicadoRepository.findAll().stream()
                .map(comunicadoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ComunicadoDTO buscarPorId(Long id) {
        Optional<Comunicado> comunicado = comunicadoRepository.findById(id);
        return comunicadoMapper.toDTO(
            comunicado.orElseThrow(() -> new EntityNotFoundException("Comunicado não encontrado com ID: " + id))
        );
    }

    public ComunicadoDTO criar(ComunicadoDTO comunicadoDTO) {
        Comunicado comunicado = comunicadoMapper.toEntity(comunicadoDTO);
        return comunicadoMapper.toDTO(comunicadoRepository.save(comunicado));
    }

    public ComunicadoDTO atualizar(Long id, ComunicadoDTO comunicadoDTO) {
        Comunicado comunicadoExistente = comunicadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comunicado não encontrado com ID: " + id));
        
        Comunicado comunicadoAtualizado = comunicadoMapper.toEntity(comunicadoDTO);
        comunicadoExistente.setTitulo(comunicadoAtualizado.getTitulo());
        comunicadoExistente.setDescricao(comunicadoAtualizado.getDescricao());
        comunicadoExistente.setSetor(comunicadoAtualizado.getSetor());
        comunicadoExistente.setCargo(comunicadoAtualizado.getCargo());
        
        return comunicadoMapper.toDTO(comunicadoRepository.save(comunicadoExistente));
    }

    public void deletar(Long id) {
        if (!comunicadoRepository.existsById(id)) {
            throw new EntityNotFoundException("Comunicado não encontrado com ID: " + id);
        }
        comunicadoRepository.deleteById(id);
    }
}
