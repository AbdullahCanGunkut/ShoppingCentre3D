package com.example.shoppingcentre3d

import android.view.LayoutInflater
import android.view.ViewGroup
import android.opengl.Matrix
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.shoppingcentre3d.databinding.FragmentRenderSurfaceFragmentBinding
import com.google.android.filament.Fence
import com.google.android.filament.IndirectLight
import com.google.android.filament.Material
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.constraintlayout.widget.ConstraintSet.Transform
import com.google.android.filament.Engine
import com.google.android.filament.Filament
import com.google.android.filament.Texture
import com.google.android.filament.gltfio.AssetLoader
import java.io.InputStream

import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.FilamentHelper
import com.google.android.filament.android.UiHelper
import com.example.shoppingcentre3d.Helpers.*

import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.example.shoppingcentre3d.ErrorHandling.ErrorCode
import com.example.shoppingcentre3d.ErrorHandling.ShoppingCentreErrorHolder
import com.example.shoppingcentre3d.ShoppingCentre3DContext.ShoppingCentre3DContext
import com.google.android.filament.gltfio.*
import kotlinx.coroutines.*
import java.io.FileOutputStream


/**
 * Tüm 3D render işlemlerini yapan fragmentimiz.
 * Tüm 3D model işlemleri burada gerçekleşiyor.
 * */
class ShoppingCenter3DRenderMachine : Fragment() {

    companion object {
        // Load the library for the utility layer, which in turn loads gltfio and the Filament core.
        init {
            Utils.init()
        }

        private const val TAG = "gltf-viewer"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment render_surface_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShoppingCenter3DRenderMachine().apply {
                arguments = Bundle().apply {

                }
            }

    }

    private lateinit var choreographer: Choreographer
    private val frameScheduler = FrameCallback()
    private lateinit var modelViewer: ModelViewer
    private lateinit var titlebarHint: TextView
    private val doubleTapListener = DoubleTapListener()
    private val singleTapListener = SingleTapListener()
    private lateinit var doubleTapDetector: GestureDetector
    private lateinit var singleTapDetector: GestureDetector
    private var remoteServer: RemoteServer? = null
    private var statusToast: Toast? = null
    private var statusText: String? = null
    private var latestDownload: String? = null
    private val automation = AutomationEngine()
    private var loadStartTime = 0L
    private var loadStartFence: Fence? = null
    private val viewerContent = AutomationEngine.ViewerContent()
    private lateinit var binding: FragmentRenderSurfaceFragmentBinding
    private lateinit var ibl: IblData

    @Entity
    private var light = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {

        binding = FragmentRenderSurfaceFragmentBinding.inflate(inflater, container, false)
        choreographer = Choreographer.getInstance()

        var contx = activity?.applicationContext

        doubleTapDetector = GestureDetector(contx, doubleTapListener)
        singleTapDetector = GestureDetector(contx, singleTapListener)
        modelViewer = ModelViewer(binding.modelRenderSurface)

        viewerContent.view = modelViewer.view
        viewerContent.sunlight = modelViewer.light
        viewerContent.lightManager = modelViewer.engine.lightManager
        viewerContent.scene = modelViewer.scene
        viewerContent.renderer = modelViewer.renderer

        binding.modelRenderSurface.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            doubleTapDetector.onTouchEvent(event)
            singleTapDetector.onTouchEvent(event)
            true
        }


        createIndirectLight()

        val view = modelViewer.view

        /*
         * Note: The settings below are overriden when connecting to the remote UI.
         */

        // on mobile, better use lower quality color buffer
        view.renderQuality = view.renderQuality.apply {
            hdrColorBuffer = View.QualityLevel.MEDIUM
        }

        // dynamic resolution often helps a lot
        view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
            enabled = true
            quality = View.QualityLevel.MEDIUM
        }

        // MSAA is needed with dynamic resolution MEDIUM
        view.multiSampleAntiAliasingOptions = view.multiSampleAntiAliasingOptions.apply {
            enabled = true
        }

        // FXAA is pretty cheap and helps a lot
        view.antiAliasing = View.AntiAliasing.FXAA

        // ambient occlusion is the cheapest effect that adds a lot of quality
        view.ambientOcclusionOptions = view.ambientOcclusionOptions.apply {
            enabled = true
        }

        // bloom is pretty expensive but adds a fair amount of realism
        view.bloomOptions = view.bloomOptions.apply {
            enabled = true
        }


        loadSettings()
        createDefaultRenderables()
        //  remoteServer = RemoteServer(8082)
        return binding.root

    }

    private fun createDefaultRenderables() {
        var instance = ShoppingCentre3DContext.getInstance()

        CoroutineScope(Dispatchers.Main).launch {
            if (instance != null && instance.currentProduct != null) {

                var model = instance.loadModel(instance.currentProduct!!.productId).await()
                if (model.errorCode == ErrorCode.Success){
                    this@ShoppingCenter3DRenderMachine.modelViewer.loadModelGlb(
                        ByteBuffer.wrap(model.returnValue as ByteArray))
                    updateRootTransform()
                }





            }
        }
        // updateRootTransform()
    }


    private fun createIndirectLight() {
        val engine = modelViewer.engine
        val scene = modelViewer.scene
        var assets = context?.assets
        if (assets != null) {
            this.ibl = IblLoader.loadIbl(assets, "studio_small_08_1k", engine)
            scene.indirectLight = this.ibl.indirectLight
            // scene.skybox = this.ibl.skybox


            val context = IBLPrefilterContext(engine)

            val specularFilter = IBLPrefilterContext.SpecularFilter(context)
            val reflections = specularFilter.run(this.ibl.skyboxTexture)

            val ibl = IndirectLight.Builder()
                .reflections(reflections)
                .intensity(30000.0f)
                .build(engine)
            scene.indirectLight = ibl
           // val sky = Skybox.Builder().environment(this.ibl.skyboxTexture).build(engine)

            scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 0.0f).build(engine)

        }

        // We now need a light, let's create a directional light
      /*  light = EntityManager.get().create()

        // Create a color from a temperature (D65)
        val (r, g, b) = Colors.cct(6_500.0f)
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(r, g, b)
            // Intensity of the sun in lux on a clear day
            .intensity(200_000.0f)
            // The direction is normalized on our behalf
            .direction(0.753f, -1.0f, -0.890f)
            .castShadows(false)
            .build(engine, light)

        // Add the entity to the scene to light it
        scene.addEntity(light)

        light = EntityManager.get().create()*/

        // Set the exposure on the camera, this exposure follows the sunny f/16 rule
        // Since we've defined a light that has the same intensity as the sun, it
        // guarantees a proper exposure

    }

    private fun readCompressedAsset(assetName: String): ByteBuffer {
        var assets = context?.assets
        if (assets != null) {
            val input = assets.open(assetName)
            val bytes = ByteArray(input.available())
            input.read(bytes)
            return ByteBuffer.wrap(bytes)
        }

        return ByteBuffer.allocate(0)
    }

    private fun setStatusText(text: String) {
        if (statusToast == null || statusText != text) {
            statusText = text
            statusToast = Toast.makeText(activity?.applicationContext, text, Toast.LENGTH_SHORT)
            statusToast!!.show()

        }
    }

    private fun clearStatusText() {
        statusToast?.let {
            it.cancel()
            statusText = null
        }
    }

    private suspend fun loadGlb(message: RemoteServer.ReceivedMessage) {
        withContext(Dispatchers.Main) {
            modelViewer.destroyModel()
            modelViewer.loadModelGlb(message.buffer)
            //   updateRootTransform()
            loadStartTime = System.nanoTime()
            loadStartFence = modelViewer.engine.createFence()
        }
    }

    private suspend fun loadHdr(message: RemoteServer.ReceivedMessage) {
        withContext(Dispatchers.Main) {
            val engine = modelViewer.engine
            val equirect = HDRLoader.createTexture(engine, message.buffer)
            if (equirect == null) {
                setStatusText("Could not decode HDR file.")
            } else {
                setStatusText("Successfully decoded HDR file.")

                val context = IBLPrefilterContext(engine)
                val equirectToCubemap = IBLPrefilterContext.EquirectangularToCubemap(context)
                val skyboxTexture = equirectToCubemap.run(equirect)!!
                engine.destroyTexture(equirect)

                val specularFilter = IBLPrefilterContext.SpecularFilter(context)
                val reflections = specularFilter.run(skyboxTexture)

                val ibl = IndirectLight.Builder()
                    .reflections(reflections)
                    .intensity(30000.0f)
                    .build(engine)

                val sky = Skybox.Builder().environment(skyboxTexture).build(engine)

                specularFilter.destroy()
                equirectToCubemap.destroy()
                context.destroy()

                // destroy the previous IBl
                engine.destroyIndirectLight(modelViewer.scene.indirectLight!!)
                engine.destroySkybox(modelViewer.scene.skybox!!)

                modelViewer.scene.skybox = sky
                modelViewer.scene.indirectLight = ibl
                viewerContent.indirectLight = ibl

            }
        }
    }

    @Synchronized
    fun syncModel() {
        var instance = ShoppingCentre3DContext.getInstance()
        if (instance != null && instance?.currentProduct != null && instance?.fileManagement != null) {
            instance?.fileManagement!!.loadModelFile(
                instance!!.currentProduct!!.productId,
                instance?.dataProvider!!
            )
        }
    }


    private suspend fun loadZip(message: RemoteServer.ReceivedMessage) {
        // To alleviate memory pressure, remove the old model before deflating the zip.
        withContext(Dispatchers.Main) {
            modelViewer.destroyModel()
        }

        // Large zip files should first be written to a file to prevent OOM.
        // It is also crucial that we null out the message "buffer" field.
        val (zipStream, zipFile) = withContext(Dispatchers.IO) {

            val file = File.createTempFile("incoming", "zip", context?.cacheDir)
            val raf = RandomAccessFile(file, "rw")
            raf.channel.write(message.buffer)
            message.buffer = null
            raf.seek(0)
            Pair(FileInputStream(file), file)
        }

        // Deflate each resource using the IO dispatcher, one by one.
        var gltfPath: String? = null
        var outOfMemory: String? = null
        val pathToBufferMapping = withContext(Dispatchers.IO) {
            val deflater = ZipInputStream(zipStream)
            val mapping = HashMap<String, Buffer>()
            while (true) {
                val entry = deflater.nextEntry ?: break
                if (entry.isDirectory) continue

                // This isn't strictly required, but as an optimization
                // we ignore common junk that often pollutes ZIP files.
                if (entry.name.startsWith("__MACOSX")) continue
                if (entry.name.startsWith(".DS_Store")) continue

                val uri = entry.name
                val byteArray: ByteArray? = try {
                    deflater.readBytes()
                } catch (e: OutOfMemoryError) {
                    outOfMemory = uri
                    break
                }
                Log.i(TAG, "Deflated ${byteArray!!.size} bytes from $uri")
                val buffer = ByteBuffer.wrap(byteArray)
                mapping[uri] = buffer
                if (uri.endsWith(".gltf") || uri.endsWith(".glb")) {
                    gltfPath = uri
                }
            }
            mapping
        }

        zipFile.delete()

        if (gltfPath == null) {
            setStatusText("Could not find .gltf or .glb in the zip.")
            return
        }

        if (outOfMemory != null) {
            setStatusText("Out of memory while deflating $outOfMemory")
            return
        }

        val gltfBuffer = pathToBufferMapping[gltfPath]!!

        // In a zip file, the gltf file might be in the same folder as resources, or in a different
        // folder. It is crucial to test against both of these cases. In any case, the resource
        // paths are all specified relative to the location of the gltf file.
        var prefix = URI(gltfPath!!).resolve(".")

        withContext(Dispatchers.Main) {
            if (gltfPath!!.endsWith(".glb")) {
                modelViewer.loadModelGlb(gltfBuffer)
            } else {
                modelViewer.loadModelGltf(gltfBuffer) { uri ->
                    val path = prefix.resolve(uri).toString()
                    if (!pathToBufferMapping.contains(path)) {
                        Log.e(
                            TAG,
                            "Could not find '$uri' in zip using prefix '$prefix' and base path '${gltfPath!!}'"
                        )
                    }
                    pathToBufferMapping[path]
                }
            }
            //    updateRootTransform()
            loadStartTime = System.nanoTime()
            loadStartFence = modelViewer.engine.createFence()
        }
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameScheduler)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameScheduler)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameScheduler)
        remoteServer?.close()
    }


    fun loadSettings() {
        //  val json = StandardCharsets.UTF_8.decode(message.buffer).toString()
        viewerContent.assetLights = modelViewer.asset?.lightEntities
        //    automation.applySettings(modelViewer.engine, json, viewerContent)
        modelViewer.view.colorGrading = automation.getColorGrading(modelViewer.engine)
        modelViewer.cameraFocalLength = automation.viewerOptions.cameraFocalLength
        modelViewer.cameraNear = automation.viewerOptions.cameraNear
        modelViewer.cameraFar = automation.viewerOptions.cameraFar
        modelViewer.camera.setExposure(16.0f, 1.0f / 125.0f, 100.0f)


        @Entity var ent = modelViewer.camera.entity

        /*   val tm = modelViewer.
           val transform = scale(Float3(1f,1f,1f)) * translation(Float3(00f , 00f , 4000f))
           tm.setTransform(tm.getInstance(ent), transpose(transform).toFloatArray())
*/
    }

    private fun updateRootTransform() {
        if (automation.viewerOptions.autoScaleEnabled) {
            modelViewer.transformToUnitCube()
        } else {
            modelViewer.clearRootTransform()
        }
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            loadStartFence?.let {
                if (it.wait(Fence.Mode.FLUSH, 0) == Fence.FenceStatus.CONDITION_SATISFIED) {
                    val end = System.nanoTime()
                    val total = (end - loadStartTime) / 1_000_000
                    Log.i(TAG, "The Filament backend took $total ms to load the model geometry.")
                    modelViewer.engine.destroyFence(it)
                    loadStartFence = null

                    val materials = mutableSetOf<Material>()
                    val rcm = modelViewer.engine.renderableManager
                    modelViewer.scene.forEach {
                        val entity = it
                        if (rcm.hasComponent(entity)) {
                            val ri = rcm.getInstance(entity)
                            val c = rcm.getPrimitiveCount(ri)
                            for (i in 0 until c) {
                                val mi = rcm.getMaterialInstanceAt(ri, i)
                                val ma = mi.material
                                materials.add(ma)
                            }
                        }
                    }
                    materials.forEach {
                        it.compile(
                            Material.CompilerPriorityQueue.HIGH,
                            Material.UserVariantFilterBit.DIRECTIONAL_LIGHTING or
                                    Material.UserVariantFilterBit.DYNAMIC_LIGHTING or
                                    Material.UserVariantFilterBit.SHADOW_RECEIVER,
                            null, null
                        )
                        it.compile(
                            Material.CompilerPriorityQueue.LOW,
                            Material.UserVariantFilterBit.FOG or
                                    Material.UserVariantFilterBit.SKINNING or
                                    Material.UserVariantFilterBit.SSR or
                                    Material.UserVariantFilterBit.VSM,
                            null, null
                        )
                    }
                }
            }

            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
                    applyAnimation(0, elapsedTimeSeconds.toFloat())
                    modelViewer.asset?.apply {
                        modelViewer.transformToUnitCube()


                    }
                }
                updateBoneMatrices()
            }

            modelViewer.render(frameTimeNanos)
            // Check if a new download is in progress. If so, let the user know with toast.
            val currentDownload = remoteServer?.peekIncomingLabel()
            if (RemoteServer.isBinary(currentDownload) && currentDownload != latestDownload) {
                latestDownload = currentDownload
                Log.i(TAG, "Downloading $currentDownload")
            }

            // Check if a new message has been fully received from the client.
            val message = remoteServer?.acquireReceivedMessage()
            if (message != null) {
                if (message.label == latestDownload) {
                    latestDownload = null
                }

            }


        }
    }

    // Just for testing purposes, this releases the current model and reloads the default model.
    inner class DoubleTapListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            modelViewer.destroyModel()
            createDefaultRenderables()
            return super.onDoubleTap(e)
        }
    }

    // Just for testing purposes
    inner class SingleTapListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            modelViewer.view.pick(
                event.x.toInt(),
                binding.modelRenderSurface.height - event.y.toInt(),
                binding.modelRenderSurface.handler,
                {
                    val name = modelViewer.asset!!.getName(it.renderable)
                    Log.v("Filament", "Picked ${it.renderable}: " + name)
                },
            )
            return super.onSingleTapUp(event)
        }
    }
}