/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.shoppingcentre3d.Helpers
import android.content.res.AssetManager
import android.graphics.BitmapFactory

import com.google.android.filament.Engine
import com.google.android.filament.IndirectLight
import com.google.android.filament.Skybox
import com.google.android.filament.Texture

import java.nio.ByteBuffer

import kotlin.math.log2


/**
 * Skybox ve Indirect Light dokularını yükelemek için kullanacağımız bir static class.
 * Skybox ve Indirect Light yüklemek için örnek bir exr dosyasını cmgen ile farklı bir biçimde oluşturmanız lazım.
 * */
data class IblData(val indirectLight: IndirectLight,
               val indirectLightTexture: Texture,
               val skybox: Skybox,
               val skyboxTexture: Texture)

class IblLoader {
    companion object {
        fun loadIbl(assets: AssetManager, name: String, engine: Engine): IblData {
            val (ibl, iblTexture) = loadIndirectLight(assets, name, engine)
            val (skybox, skyboxTexture) = loadSkybox(assets, name, engine)
            return IblData(ibl, iblTexture, skybox, skyboxTexture)
        }

        fun destroyIbl(engine: Engine, ibl: IblData) {
            engine.destroySkybox(ibl.skybox)
            engine.destroyTexture(ibl.skyboxTexture)
            engine.destroyIndirectLight(ibl.indirectLight)
            engine.destroyTexture(ibl.indirectLightTexture)
        }

        private fun peekSize(assets: AssetManager, name: String): Pair<Int, Int> {
            assets.open(name).use { input ->
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, opts)
                return opts.outWidth to opts.outHeight
            }
        }

        private fun loadIndirectLight(
            assets: AssetManager,
            name: String,
            engine: Engine
        ): Pair<IndirectLight, Texture> {
            val (w, h) = peekSize(assets, "environments/$name/m0_nx.rgb32f")
            val texture = Texture.Builder()
                .width(w)
                .height(h)
                .levels(log2(w.toFloat()).toInt() + 1)
                .format(Texture.InternalFormat.R11F_G11F_B10F)
                .sampler(Texture.Sampler.SAMPLER_CUBEMAP)
                .build(engine)

            for (i in 0 until texture.levels) {
                if (!loadCubemap(texture, assets, name, engine, "m${i}_", i)) break
            }

            return IndirectLight.Builder()
                .reflections(texture)
                .intensity(3000_000.0f)
                .build(engine) to texture
        }

        private fun loadSkybox(
            assets: AssetManager,
            name: String,
            engine: Engine
        ): Pair<Skybox, Texture> {
            val (w, h) = peekSize(assets, "environments/$name/nx.rgb32f")
            val texture = Texture.Builder()
                .width(w)
                .height(h)
                .levels(9)
                .format(Texture.InternalFormat.R11F_G11F_B10F)
                .sampler(Texture.Sampler.SAMPLER_CUBEMAP)
                .build(engine)

            loadCubemap(texture, assets, name, engine)

            return Skybox.Builder().environment(texture).build(engine) to texture
        }

        private fun loadCubemap(
            texture: Texture,
            assets: AssetManager,
            name: String,
            engine: Engine,
            prefix: String = "",
            level: Int = 0
        ): Boolean {
            // This is important, the alpha channel does not encode opacity but some
            // of the bits of an R11G11B10F image to represent HDR data. We must tell
            // Android to not premultiply the RGB channels by the alpha channel
            val opts = BitmapFactory.Options().apply { inPremultiplied = false }

            // R11G11B10F is always 4 bytes per pixel
            val faceSize = texture.getWidth(level) * texture.getHeight(level) * 4
            val offsets = IntArray(6) { it * faceSize }
            // Allocate enough memory for all the cubemap faces
            val storage = ByteBuffer.allocateDirect(faceSize * 6)

            arrayOf("px", "nx", "py", "ny", "pz", "nz").forEach { suffix ->
                try {
                    assets.open("environments/$name/$prefix$suffix.rgb32f").use {
                        val bitmap = BitmapFactory.decodeStream(it, null, opts)
                        bitmap?.copyPixelsToBuffer(storage)
                    }
                } catch (e: Exception) {
                    return false
                }
            }

            // Rewind the texture buffer
            storage.flip()

            val buffer = Texture.PixelBufferDescriptor(
                storage,
                Texture.Format.RGB, Texture.Type.UINT_10F_11F_11F_REV
            )
            texture.setImage(engine, level, buffer, offsets)

            return true
        }
    }
}