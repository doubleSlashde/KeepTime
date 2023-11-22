package de.doubleslash.keeptime.REST_API.controller;
//import de.doubleslash.keeptime.model.Work;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.springframework.stereotype.Component;
//
//@Mapper(componentModel = "spring")
//@Component
//public interface WorkMapper {
//
//   @Mapping(source = "project.id", target = "projectId")
//   @Mapping(source = "project.color", target = "color")
//   WorkDTO workToWorkDTO(Work work);
//}
//   @Mapping(target = "id", ignore = true)
//   @Mapping(target = "project", ignore = true)
//   Work updateWorkFromDTO(WorkDTO workDTO, @MappingTarget Work existingWork);
//}


import de.doubleslash.keeptime.model.Work;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WorkMapper {

   WorkMapper INSTANCE = Mappers.getMapper(WorkMapper.class);

   @Mapping(source = "project", target = "project")
   WorkDTO workToWorkDTO(Work work);

   @Mapping(source = "project", target = "project")
   Work workDTOToWork(WorkDTO workDTO);
}


