package org.cs.shutters

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment


@Configuration
@Import(SunShadeRuleConfiguration.Registrar::class)
class SunShadeRuleConfiguration {

    /**
     * Dynamic bean registration based on property values.
     */
    class Registrar(private val environment: Environment) : ImportBeanDefinitionRegistrar {
        override fun registerBeanDefinitions(
            importingClassMetadata: org.springframework.core.type.AnnotationMetadata,
            registry: org.springframework.beans.factory.support.BeanDefinitionRegistry,
        ) {
            var i = 0;
            while (environment.containsProperty("shutters.rules.sunShades[$i].deviceIds")) {
                // since we are very early in the lifecycle, we have to do the conversion ourselves
                // the properties here also respect environment variables overrides
                val sunShadeConfig = ShuttersProperties.Rules.SunShade(
                    deviceIds = environment.getProperty("shutters.rules.sunShades[$i].deviceIds")?.split(",")
                        ?: emptyList(),
                    targetShadePosition = environment.getProperty("shutters.rules.sunShades[$i].targetShadePosition")
                        ?.toInt() ?: 0,
                    minAzimuth = environment.getProperty("shutters.rules.sunShades[$i].minAzimuth")?.toDouble() ?: 0.0,
                    maxAzimuth = environment.getProperty("shutters.rules.sunShades[$i].maxAzimuth")?.toDouble() ?: 0.0,
                    minAltitude = environment.getProperty("shutters.rules.sunShades[$i].minAltitude")?.toDouble()
                        ?: 0.0,
                    minTempInC = environment.getProperty("shutters.rules.sunShades[$i].minTempInC")?.toDouble() ?: 0.0,
                    maxCloudiness = environment.getProperty("shutters.rules.sunShades[$i].maxCloudiness")?.toInt() ?: 0,
                )

                val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SunShadeRule::class.java)
                    .addConstructorArgValue(environment.getProperty("shutters.latitude")?.toDouble())
                    .addConstructorArgValue(environment.getProperty("shutters.longitude")?.toDouble())
                    .addConstructorArgValue(sunShadeConfig)
                    .addConstructorArgReference("weatherApiClient")
                    .beanDefinition

                registry.registerBeanDefinition("sunShadeRule-$i", beanDefinition)

                i++
            }
        }
    }
}
