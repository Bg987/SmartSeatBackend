package com.example.SmartSeatBackend.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CacheUtil {


    private final  CacheManager cacheManager;
    private final  HelperMethods helper;


    public void deleteCache() throws Exception {

        String role = helper.getRole();
        if(role==null){
            return;
        }
        if (role.contains("college")) {
            Long collegeId = helper.getCollegeIdByUserId();
            delete("studentsByCollege", String.valueOf(collegeId));

        }
        else if (role.contains("university")) {
            delete("colleges",null);
            delete("subjects",null);
        }
    }

    public void delete(String name, Object key) {
        Cache cache = cacheManager.getCache(name);
        if (cache == null) return;

        if (key == null) {
            cache.clear(); // Wipes the whole bucket

        } else {
            cache.evict(key); // Wipes ONLY the specific ID

        }
    }
}