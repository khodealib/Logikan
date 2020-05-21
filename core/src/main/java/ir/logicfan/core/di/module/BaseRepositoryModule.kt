package ir.logicfan.core.di.module

import dagger.Module
import dagger.Provides
import dagger.Reusable
import ir.logicfan.core.data.util.ReactiveUtil
import ir.logicfan.core.data.network.endpoint.DummyApi
import ir.logicfan.core.data.network.endpoint.GenericApi
import ir.logicfan.core.data.reactive.ASyncTransformer
import ir.logicfan.core.domain.repository.DummyRepository
import ir.logicfan.core.data.repository.DummyRepositoryImpl
import ir.logicfan.core.domain.repository.GenericRepository
import ir.logicfan.core.data.repository.GenericRepositoryImpl

@Module
abstract class BaseRepositoryModule {

    companion object {
        @Provides
        @Reusable
        fun dummyRepository(dummyApi: DummyApi): DummyRepository = DummyRepositoryImpl(
            dummyApi,
            ReactiveUtil(),
            ReactiveUtil()
        )

        @Provides
        @Reusable
        fun genericRepository(genericApi: GenericApi): GenericRepository = GenericRepositoryImpl(
            genericApi, ASyncTransformer()
        )
    }
}