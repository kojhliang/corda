package net.corda.serialization.internal.amqp

import java.io.NotSerializableException

/**
 * An implementation of [EvolutionSerializerProvider] that disables all evolution within a
 * [SerializerFactory]. This is most useful in testing where it is known that evolution should not be
 * occurring and where bugs may be hidden by transparent invocation of an [EvolutionSerializer]. This
 * prevents that by simply throwing an exception whenever such a serializer is requested.
 */
object FailIfEvolutionAttempted : EvolutionSerializerProvider {
    override fun getEvolutionSerializer(factory: LocalSerializerFactory,
                                        descriptorBasedSerializerRegistry: DescriptorBasedSerializerRegistry,
                                        typeNotation: TypeNotation,
                                        newSerializer: AMQPSerializer<Any>,
                                        schemas: SerializationSchemas): AMQPSerializer<Any> {
        throw NotSerializableException("No evolution should be occurring\n" +
                "    ${typeNotation.name}\n" +
                "        ${typeNotation.descriptor.name}\n" +
                "    ${newSerializer.type.typeName}\n" +
                "        ${newSerializer.typeDescriptor}\n\n${schemas.schema}")
    }
}
