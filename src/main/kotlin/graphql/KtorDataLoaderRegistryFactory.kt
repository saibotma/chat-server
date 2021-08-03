package graphql

import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import org.dataloader.DataLoaderRegistry

class KtorDataLoaderRegistryFactory : DataLoaderRegistryFactory {

    override fun generate(): DataLoaderRegistry {
        val registry = DataLoaderRegistry()
        // TODO(saibotma): Register some data loaders
        /*registry.register(UniversityDataLoader.dataLoaderName, UniversityDataLoader.getDataLoader())
        registry.register(CourseDataLoader.dataLoaderName, CourseDataLoader.getDataLoader())
        registry.register(BookDataLoader.dataLoaderName, BookDataLoader.getDataLoader())*/
        return registry
    }
}
