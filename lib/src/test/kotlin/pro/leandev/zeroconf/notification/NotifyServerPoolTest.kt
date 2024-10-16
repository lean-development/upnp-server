package pro.leandev.zeroconf.notification

import kotlinx.coroutines.Dispatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import java.net.NetworkInterface
import kotlin.coroutines.CoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotifyServerPoolTest {

    private lateinit var networkInterface1: NetworkInterface
    private lateinit var networkInterface2: NetworkInterface
    private lateinit var notifyServer1: NotifyServer
    private lateinit var notifyServer2: NotifyServer
    private lateinit var coroutineContext: CoroutineContext

    private lateinit var notifyServerPool: NotifyServerPool

    @BeforeTest
    fun setUp() {
        networkInterface1 = mock(NetworkInterface::class.java)
        `when`(networkInterface1.displayName).thenReturn("eth0")

        networkInterface2 = mock(NetworkInterface::class.java)
        `when`(networkInterface2.displayName).thenReturn("eth1")

        coroutineContext = Dispatchers.Default

        // Use partial mocking to control NotifyServer creation
        notifyServerPool = spy(
            NotifyServerPool(
                "testCPName",
                listOf(networkInterface1, networkInterface2),
                coroutineContext
            )
        )

        notifyServer1 = spy(notifyServerPool.getPool()["eth0"]!!)
        notifyServer2 = spy(notifyServerPool.getPool()["eth1"]!!)

        // Replace pool with mocks
        notifyServerPool.setPool(mapOf("eth0" to notifyServer1, "eth1" to notifyServer2))
    }

    @Test
    fun `test pool initialization`() {
        val pool = notifyServerPool.getPool()
        assertEquals(2, pool.size)                  // Verify that the pool has been created correctly
    }

    @Test
    fun `test start all servers`() {
        notifyServerPool.start()

        verify(notifyServer1).start()
        verify(notifyServer2).start()
    }

    @Test
    fun `test start specific servers`() {
        // Call start with specific interface names
        notifyServerPool.start(listOf("eth0"))

        // Verify start() was only called on eth0
        verify(notifyServer1).start()
        verify(notifyServer2, never()).start()
    }

    @Test
    fun `test stop all servers`() {
        // Call stop without specifying interface names
        notifyServerPool.stop()

        // Verify stop() was called on all NotifyServer instances
        verify(notifyServer1).stop()
        verify(notifyServer2).stop()
    }

    @Test
    fun `test stop specific servers`() {
        // Call stop with specific interface names
        notifyServerPool.stop(listOf("eth1"))

        // Verify stop() was only called on eth1
        verify(notifyServer1, never()).stop()
        verify(notifyServer2).stop()
    }

    @Test
    fun `test search on all servers`() {
        // Call search without specifying an interface
        notifyServerPool.search("target")

        // Verify search() was called on all NotifyServer instances
        verify(notifyServer1).search("target")
        verify(notifyServer2).search("target")
    }

    @Test
    fun `test search with null target`() {
        // Call search with a null target
        notifyServerPool.search(null)

        // Verify search() was called with null target on all NotifyServer instances
        verify(notifyServer1).search(null)
        verify(notifyServer2).search(null)
    }
}