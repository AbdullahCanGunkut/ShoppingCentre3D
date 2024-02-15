package com.example.shoppingcentre3d.Helpers

import java.time.*



/**
 * Şuan bu test edilmedi ve sonraki geliştirme aşamalarında gelecek eğer bir sıkıntı olamaz ise verileri Cache yapıp daha düzenli ve daha performanslı
 * bir şekilde yüklemek için kullanılacak ama şuan geçerli değil.
 *
 * */

//Herhangi bir nesnenin (özellike dosyalarda kullanacağız) oluşma tarihi ve kendisi ile tutan kısa bir class.
//data class ConciseDateObject<ObjectType , DateTime : LocalDateTime>(var obj : ObjectType , var date : DateTime)
typealias  ConciseDateObject<ObjectType, DateTime> = Pair<ObjectType, DateTime>



class CacheableMapKey<Key : String, DateTime : LocalDateTime>(
    val key: Key,
    var dateTime: DateTime
) {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is CacheableMapKey<*, *> -> this.key == other.key
            else -> false
        }

    }
}

//CacheMap'i özel geliştirdim özellikle internetten indirilen verileri daha kolay bilgisayar ile güncel olarak eşitlemek için.
class CacheableMap<Key : String, DateTime : LocalDateTime> :
    HashMap<CacheableMapKey<Key, DateTime>, Any> {

    constructor() : super() {
    }

    fun update(
        cache: CacheableMap<Key, DateTime>,
        updateItem: ((CacheableMapKey<Key, DateTime>, value: Any) -> Any)?
    ) {
        for (entry in cache.entries) {
            var finish: Boolean = false
            for (key in this.keys) {
                if (key == entry.key && key.dateTime != entry.key.dateTime) { //Var olan bir verinin keyleri eşleşiyor ve tarihleri farklı ise ozaman günecelleme var olan veri üzerinde.
                    this.remove(key)
                    if (updateItem != null)
                        this.put(entry.key, updateItem(entry.key, entry.value))
                    else
                        this.put(entry.key, entry.value)
                    break
                }
            }

            if (!finish) {//Eğer yeni bir değer geldi ise zaten key olmayacaktır ve o yüzden cache'e yeni değer eklemiş oluruz.
                if (updateItem != null)
                    this.put(entry.key, updateItem(entry.key, entry.value))
                else
                    this.put(entry.key, entry.value)
            }
        }
    }
}
