package io.legere.convention

import org.gradle.api.provider.Property

abstract class PublishPluginExtension {
    abstract val artifactId: Property<String>
    abstract val name: Property<String>
    abstract val description: Property<String>
    // Add other configurable properties here if needed
}
