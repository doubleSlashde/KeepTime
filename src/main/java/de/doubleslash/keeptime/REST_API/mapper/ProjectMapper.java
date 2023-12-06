// Copyright 2023 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.REST_API.mapper;

import de.doubleslash.keeptime.REST_API.DTO.ProjectColorDTO;
import de.doubleslash.keeptime.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ColorMapper.class)
public interface ProjectMapper {

   ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

   ProjectColorDTO projectToProjectDTO(Project project);

   Project projectDTOToProject(ProjectColorDTO projectColorDTO);
}
