package com.xebia.resources;

import com.xebia.annotations.Secured;
import com.xebia.dao.UserDAO;
import com.xebia.dto.*;
import com.xebia.entities.*;
import com.xebia.exception.ApplicationException;
import com.xebia.exception.FileException;
import com.xebia.services.IAssetService;
import com.xebia.services.excel.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pgupta on 23-07-2016.
 */
@Component
@Secured
@Path("/asset")
public class AssetResource {

    @Autowired
    IAssetService assetService;

    @Context
    HttpServletRequest httpServletRequest;


    @Autowired
    ExcelService excelService;

    @Autowired
    UserDAO userDAO;
    
    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("createAssetType")
    @Produces("application/text")
    @Consumes("application/json")
    public String createAssetType(@RequestBody AssetType assetType){
        return assetService.createAssetType(assetType, httpServletRequest.getHeader("UserName"));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("fetchAllAssetType")
    @Produces("application/json")
    public ActionResult getAllAssetType(){
        ActionResult result = new ActionResult();
        Map resultMap = assetService.getAllAssetType();
        result.setStatus(ActionResult.Status.SUCCESS);
        result.addData("Type", resultMap.get("AssetDTOResult"));
        result.addData("ManuMap", resultMap.get("ManufactureMap"));
        return result;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("deleteAssetType")
    @Produces("application/text")
    public String deleteAssetType(@RequestBody String id){
        return assetService.deleteAssetType(id, httpServletRequest.getHeader("UserName"));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("updateAssetType")
    @Produces("application/text")
    public String updateAssetType(@RequestBody AssetType assetType){
        return assetService.updateAssetType(assetType, httpServletRequest.getHeader("Username"));
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("createAsset")
    @Produces("application/text")
    @Consumes("application/json")
    public String createAsset(@RequestBody AssetDto asset){
        return assetService.createAsset(asset);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("fetchAllAsset")
    @Produces("application/json")
    public List<Asset> getAllAsset(@QueryParam("offset") int offset, @QueryParam("limit") int limit){
        return assetService.getAllAsset(offset, limit);
    }

    @GET
    @Path("getAssetCount")
    @Produces("application/json")
    public long getAssetCount(){
        return assetService.getAssetsCount();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("fetchAssetByType")
    @Produces("application/json")
    public List<Asset> getAsset(@RequestBody BigInteger typeId){
        return assetService.getAssetByType(typeId);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("fetchAvailableAssetByTypeAndManu")
    @Produces("application/json")
    public List<Asset> fetchAvailableAssetByTypeAndManu(@RequestBody String ids){
        String[] idArray = ids.split("-");
        BigInteger typeId = new BigInteger(idArray[0]);
        BigInteger manuId = new BigInteger(idArray[1]);
        return assetService.fetchAvailableAssetByTypeAndManu(typeId, manuId);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("deleteAsset")
    @Produces("application/json")
    public ActionResult deleteAsset(@RequestBody String id){
        ActionResult result = new ActionResult();
        try{
            assetService.deleteAsset(id);
            result.setStatus(ActionResult.Status.SUCCESS);
        }
        catch(ApplicationException e){
            String errorCode = e.getMessage().split(":")[0];
            String employeeName = e.getMessage().split(":")[1];
            result.addData("eCode", errorCode);
            result.addData("empName", employeeName);
            result.setStatus(ActionResult.Status.FAILURE);
        }
        return result;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("assignAsset")
    @Produces("application/text")
    public String assignAsset(@RequestBody AssetDto assetDto){
        return assetService.assignAsset(assetDto);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("unAssignAsset")
    @Produces("application/text")
    public String unAssignAsset(@RequestBody AssetDto assetDto){
        String username = httpServletRequest.getHeader("Username");
        assetDto.setUserName(username);
        return assetService.unAssignAsset(assetDto);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("updateAsset")
    @Produces("application/text")
    public String updateAsset(@RequestBody Asset asset){
        return assetService.updateAsset(asset);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("fetchEmployeeAsset")
    @Produces("application/json")
    public List<AssetDto> fetchAssetByEmployee(@RequestBody BigInteger employeeID){
        return assetService.getEmployeeAsset(employeeID);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("createAssetManufacturer")
    @Produces("application/text")
    @Consumes("application/json")
    public String createAssetManufacturer(@RequestBody AssetManufacturer assetManufacturer){
        return assetService.createAssetManufacturer(assetManufacturer, httpServletRequest.getHeader("Username"));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("fetchAllAssetManufacturer")
    @Produces("application/json")
    public List<AssetManufacturer> getAllAssetManufacturer(){
        return assetService.getAllAssetManufacturer();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("deleteAssetManufacturer")
    @Produces("application/json")
    public ActionResult deleteAssetManufacturer(@RequestBody String id){
        ActionResult result = new ActionResult();
        try{
            assetService.deleteAssetManufacturer(id, httpServletRequest.getHeader("Username"));
            result.setStatus(ActionResult.Status.SUCCESS);
            return result;

        }
        catch (Exception  e){
            result.setStatus(ActionResult.Status.FAILURE);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("eMessage", e.getMessage());
            result.setError(errorMap);
        }
        return result;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("updateAssetManufacturer")
    @Produces("application/text")
    public String updateAssetManufacturer(@RequestBody AssetManufacturer assetManufacturer){
        return assetService.updateAssetManufacturer(assetManufacturer, httpServletRequest.getHeader("Username"));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("fetchAllConfiguration")
    @Produces("application/json")
    public List<HardwareConfiguration> fetchHarwareConfigurations(){
        return assetService.getHardwareConfigurations();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("createHardwareConfigurations")
    @Produces("application/text")
    @Consumes("application/json")
    public String createHarwareConfiguration(@RequestBody HardwareConfiguration hardwareConfiguration){
        return assetService.createHardwareConfig(hardwareConfiguration);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("updateHardwareConfigurations")
    @Produces("application/text")
    @Consumes("application/json")
    public String updateHarwareConfiguration(@RequestBody HardwareConfiguration hardwareConfiguration){
        return assetService.updateHardwareConfig(hardwareConfiguration);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("deleteHardwareConfiguration")
    @Produces("application/json")
    public ActionResult deleteHardwareConfiguration(@RequestBody String id){
        ActionResult result = new ActionResult();
        try {
            result.setStatus(ActionResult.Status.SUCCESS);
            assetService.deleteHardwareConfig(id);
        }
        catch(ApplicationException e){
            result.setStatus(ActionResult.Status.FAILURE);
        }
        return result;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @POST
    @Path("getAssetsHistory")
    @Produces("application/json")
    public List<AssetHistoryDTO> getAssetHistory(@RequestBody BigInteger employeeId){
        return assetService.getAssetsHistory(employeeId);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("getAssetExpirationReport")
    @Produces("application/json")
    public List<AssetDto> getAssetExpirationReport(){
        return assetService.getExpirationReportForDashboard();
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("generateAssetReport")
    @Produces("application/vnd.ms-excel")
    public Response generateAssetReport(){

        File file = excelService.exportToExcel();

        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=new-excel-file.xls");
        return response.build();
    }

    @POST
    @Path("getEmployeeAssetsFileParam")
    @Produces("application/pdf")
    public Response getEmployeeAssetsFileParam(@RequestBody BigInteger employeeId){
        File file = assetService.getEmployeeAssetsFileParam(employeeId,httpServletRequest.getHeader("Username"));

        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=new-pdf-file.pdf");
        return response.build();
    }

    @POST
    @Path("searchAsset")
    @Produces("application/json")
    public ActionResult searchAsset(@RequestBody AssetDto assetDto){
        ActionResult result = new ActionResult();
        Map resultMap =  assetService.searchAsset(assetDto);
        result.addData("result", resultMap.get("result"));
        result.addData("count", resultMap.get("count"));
        return result;
    }

    @GET
    @Path("processHistoricalAssets")
    @Produces("application/json")
    public ActionResult processHistoricalAssets(){
        ActionResult result = new ActionResult();
        try{
            assetService.processHistoricalAssets();
            result.setStatus(ActionResult.Status.SUCCESS);
        }
        catch (FileException f){
            result.setStatus(ActionResult.Status.FAILURE);
        }
        return result;
    }

    @GET
    @Path("getAssets")
    @Produces("application/json")
    public List<AssetDto> getAssets(){
        String userName = httpServletRequest.getHeader("Username");
        User user = userDAO.getUserByUName(userName);
        Employee employee = user.getEmployee();
        return assetService.getEmployeeAsset(employee.getId());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GET
    @Path("getAssetsHistoryLoggedIn")
    @Produces("application/json")
    public List<AssetHistoryDTO> getAssetsHistoryLoggedIn(){
        String userName = httpServletRequest.getHeader("Username");
        User user = userDAO.getUserByUName(userName);
        Employee employee = user.getEmployee();
        return assetService.getAssetsHistory(employee.getId());
    }

    @POST
    @Path("assetApproval")
    @Consumes("application/json")
    @Produces("application/json")
    public ActionResult requestAsset(@RequestBody AssetRequestDTO assetRequestDTO){
        ActionResult actionResult = new ActionResult();
        String userNamme = httpServletRequest.getHeader("Username");
        assetRequestDTO.setUserName(userNamme);
        assetService.requestAsset(assetRequestDTO);
        actionResult.setStatus(ActionResult.Status.SUCCESS);
        return actionResult;
    }

    @GET
    @Path("getApprovals")
    @Produces("application/json")
    public List<AssetApprovalDTO> fetchAssetApprovals(){
        String userNamme = httpServletRequest.getHeader("Username");
        return assetService.fetchAssetApprovals(userNamme);
    }

    @GET
    @Path("getAllApprovals")
    @Produces("application/json")
    public List<AssetApprovalDTO> fetchAllAssetApprovals(){
        return assetService.fetchAssetApprovals(null);
    }

    @GET
    @Path("getApprovalsBadgeCount")
    @Produces("application/text")
    public String getApprovalsBadgeCount(){
        return assetService.getApprovalsBadgeCount();
    }

    @POST
    @Path("searchApprovals")
    @Produces("application/json")
    public List<AssetApprovalDTO> searchApprovals(@RequestBody AssetApprovalDTO assetApprovalDTO){
        return assetService.searchApprovals(assetApprovalDTO);
    }

    @POST
    @Path("updateApproval")
    @Produces("application/text")
    public String updateApproval(@RequestBody AssetApprovalDTO assetApprovalDTO){
        assetApprovalDTO.setModifiedBy(httpServletRequest.getHeader("Username"));
        assetService.updateAssetApproval(assetApprovalDTO);
        return "OK";
    }

    @GET
    @Path("/fetchPendingApprovals")
    @Produces("application/json")
    public List<AssetApprovalDTO> getPendingApprovals(){
        return assetService.fetchPendingApprovals();
    }


}
