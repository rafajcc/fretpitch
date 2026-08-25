package com.fretpitch.di

import com.fretpitch.data.audio.PitchDetectorImpl
import com.fretpitch.domain.repository.PitchDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPitchDetector(impl: PitchDetectorImpl): PitchDetector
}
