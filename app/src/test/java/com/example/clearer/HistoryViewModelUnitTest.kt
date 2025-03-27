package com.example.clearer

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clearer.adapter.PracticeAdapter
import com.example.clearer.models.Practice
import com.example.clearer.vms.HistoryFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime


@RunWith(MockitoJUnitRunner::class)
class HistoryViewModelUnitTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth
    @Mock
    private lateinit var mockFire: FirebaseFirestore
    @Mock
    private lateinit var mockUser: FirebaseUser

    private lateinit var model: HistoryFragmentViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.lenient().`when`(mockAuth.currentUser).thenReturn(mockUser)
        Mockito.lenient().`when`(mockUser.uid).thenReturn("test")
        model = HistoryFragmentViewModel(mockAuth, mockFire, mockUser.uid)
    }

    @Test
    fun testSortList_NewestFirst() {
        // Arrange
        val practiceList : ArrayList<Practice> = ArrayList()
        practiceList.add(Practice("Self Assessment", "problem1",
            "123", false, LocalDateTime.of(2023, 12,
                4, 15, 30, 23)))
        practiceList.add(Practice("Self Assessment", "problem2",
            "123", false, LocalDateTime.of(2023, 5,
                12, 15, 30, 23)))
        model.practiceList = practiceList

        // Act
        model.sortListByDate("Newest First")

        // Assert
        assertEquals(practiceList, practiceList.sortedByDescending { it.date?.dayOfYear })
    }

    @Test
    fun testSortList_OldestFirst() {
        // Arrange
        val practiceList : ArrayList<Practice> = ArrayList()
        practiceList.add(Practice("Self Assessment", "problem2",
            "123", false, LocalDateTime.of(2023, 5,
                12, 15, 30, 23)))
        practiceList.add(Practice("Self Assessment", "problem1",
            "123", false, LocalDateTime.of(2023, 12,
                4, 15, 30, 23)))
        model.practiceList = practiceList

        // Act
        model.sortListByDate("Oldest First")

        // Assert
        assertEquals(practiceList, practiceList.sortedBy { it.date?.dayOfYear })
    }

}