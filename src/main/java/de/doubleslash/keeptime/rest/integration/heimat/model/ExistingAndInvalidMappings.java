package de.doubleslash.keeptime.rest.integration.heimat.model;

import de.doubleslash.keeptime.controller.HeimatController;

import java.util.List;

public record ExistingAndInvalidMappings(List<HeimatController.ProjectMapping> validMappings,
                                         List<String> invalidMappingsAsString) {}
