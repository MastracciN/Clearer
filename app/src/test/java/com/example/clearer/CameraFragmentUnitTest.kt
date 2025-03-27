package com.example.clearer

import com.example.clearer.fragments.CameraFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CameraFragmentUnitTest {

    private lateinit var cameraFragment: CameraFragment

    @Before
    fun setup(){
        cameraFragment = CameraFragment()
    }

    @Test
    fun hasWords_True(){
        // Arrange & Act
        val result = cameraFragment.hasWords("a string")
        // Assert
        assert(result)
    }

    @Test
    fun hasWords_False(){
        // Arrange & Act
        val result = cameraFragment.hasWords("")
        // Assert
        assert(!result)
    }
}