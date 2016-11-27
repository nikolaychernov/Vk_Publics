package com.nikolaychernov.vkpublics;

import com.vk.sdk.api.model.VKApiPhoto;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Nikolay on 26.11.2016.
 */

public class VkUtils {
    public static String findLargestPhotoUrl(VKApiPhoto photo) {
        if (StringUtils.isNotEmpty(photo.photo_2560)) return photo.photo_2560;
        if (StringUtils.isNotEmpty(photo.photo_1280)) return photo.photo_1280;
        if (StringUtils.isNotEmpty(photo.photo_807)) return photo.photo_807;
        if (StringUtils.isNotEmpty(photo.photo_604)) return photo.photo_604;
        if (StringUtils.isNotEmpty(photo.photo_130)) return photo.photo_130;
        if (StringUtils.isNotEmpty(photo.photo_75)) return photo.photo_75;
        return null;
    }
}
