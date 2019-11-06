package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.service.PluginPackageDataModelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api")
public class PluginPackageDataModelController {

    @Autowired
    private PluginPackageDataModelServiceImpl pluginPackageDataModelService;

    @GetMapping("/models")
    @ResponseBody
    public JsonResponse getAllDataModels() {
        List<PluginPackageEntityDto> allPluginPackageEntityList;
        try {
            allPluginPackageEntityList = pluginPackageDataModelService.overview();
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }

    @GetMapping("/models/package/{plugin-package-id}")
    @ResponseBody
    public JsonResponse pullDynamicDataModel(@PathVariable(value = "plugin-package-id") int pluginPackageId) {
        PluginPackageDataModelDto pluginPackageDataModelDto = pluginPackageDataModelService.pullDynamicDataModel(pluginPackageId);
        return JsonResponse.okayWithData(null);
    }

    @GetMapping("/packages/{id}/models")
    @ResponseBody
    public JsonResponse getDataModelByPackageId(@PathVariable(value = "id") int packageId) {
        List<PluginPackageEntityDto> allPluginPackageEntityList;
        try {
            allPluginPackageEntityList = pluginPackageDataModelService.packageView(packageId);
        } catch (WecubeCoreException ex) {
            return JsonResponse.error(ex.getMessage());
        }
        return JsonResponse.okayWithData(allPluginPackageEntityList);
    }
}
