package de.doubleslash.keeptime.REST_API.mapper;

import de.doubleslash.keeptime.REST_API.ProjectColorDTO;
import de.doubleslash.keeptime.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ColorMapper.class)
public interface ProjectMapper {

   ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

   ProjectColorDTO projectToProjectDTO(Project project);

   Project projectDTOToProject(ProjectColorDTO projectColorDTO);
}
