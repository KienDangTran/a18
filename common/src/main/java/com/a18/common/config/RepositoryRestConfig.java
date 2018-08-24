package com.a18.common.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class RepositoryRestConfig extends RepositoryRestConfigurerAdapter {

  @Override
  public void configureConversionService(ConfigurableConversionService conversionService) {
    super.configureConversionService(conversionService);
    conversionService.addConverter(String.class, LocalDate.class, LocalDate::parse);
    conversionService.addConverter(String.class, LocalDateTime.class, LocalDateTime::parse);
  }

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    super.configureRepositoryRestConfiguration(config);
    exposeIdForDomainObjects(config);
  }

  private void exposeIdForDomainObjects(RepositoryRestConfiguration config) {
    final ClassPathScanningCandidateComponentProvider provider =
        new ClassPathScanningCandidateComponentProvider(false);

    provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

    final Set<BeanDefinition> beans =
        provider.findCandidateComponents("com.a18.*.model");

    beans.forEach(beanDefinition -> {
      Class<?> idExposedClasses;
      try {
        idExposedClasses = Class.forName(beanDefinition.getBeanClassName());
        config.exposeIdsFor(Class.forName(idExposedClasses.getName()));
      } catch (ClassNotFoundException e) {
        // Can't throw ClassNotFoundException due to the method signature. Need to cast it
        throw new RuntimeException("Failed to expose `id` field due to", e);
      }
    });
  }
}
