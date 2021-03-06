package com.xebia.services.impl;

import com.xebia.common.StringUtils;
import com.xebia.common.Utility;
import com.xebia.dao.*;
import com.xebia.dto.EmployeeSearchDTO;
import com.xebia.entities.*;
import com.xebia.enums.*;
import com.xebia.exception.ApplicationException;
import com.xebia.messaging.JMSMailService;
import com.xebia.services.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Pgupta on 13-08-2016.
 */
@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Autowired
    EmployeeDAO employeeDAO;

    @Autowired
    AssetDAO assetDAO;

    @Autowired
    AssetHistoryDAO assetHistoryDAO;

    @Autowired
    AssignAssetMailDAO assignAssetMailDAO;

    @Autowired
    JMSMailService jmsMailService;

    @Autowired
    UserDAO userDAO;

    public void delete(String eCode, String username) throws ApplicationException {
        EmployeeSearchDTO employee = new EmployeeSearchDTO();
        employee.setECode(eCode);
        employee.setApprovalsRequired("NA");
        List<Employee> employeeList = (List) (employeeDAO.getByEmployeeObject(employee).get("result"));
        if (employeeList.size() == 0) {
            throw new ApplicationException("NOT_FOUND");
        }
        User loggedInUser = userDAO.getUserByUName(username);

        if (StringUtils.areBothNotBlankAndEqual.test(loggedInUser.getEmployee().getECode(),eCode)) {
            throw new ApplicationException("LOGGEDIN_USER_ERROR");
        }
        List<Asset> employeeAssets = assetDAO.getByEmployeeId(employeeList.get(0).getId());

        if (employeeAssets.size() > 0) {
            throw new ApplicationException("ASSETS_PRESENT");
        }

        List<AssetHistory> assetHistoryList = assetHistoryDAO.getHistoryByEmployee(employeeList.get(0).getId());
        for (AssetHistory assetHistory : assetHistoryList) {
            assetHistoryDAO.delete(assetHistory);
        }

        List<AssignAssetMail> assignAssetMails = assignAssetMailDAO.getMailObjectsByEmployeeId(employeeList.get(0));
        for (AssignAssetMail assignAssetMail : assignAssetMails) {
            assignAssetMailDAO.delete(assignAssetMail);
        }

        Employee emp = employeeList.get(0);
        BigInteger empId = emp.getId();
        employeeDAO.markDeleted(emp);
        jmsMailService.registerToMailQueue(
                Utility.createEventMailObject(loggedInUser.getId(),
                        EventEnum.DELETE.toString(),
                        EventType.EMP.toString(),
                        empId));
    }

    public void create(Employee employee, String userName) throws ApplicationException {
        if (isEmployeeAlreadyPresent.test(employee.getECode())) {
            throw new ApplicationException("INVALID_EMP_CODE");
        } else {
            if (StringUtils.areBothNotBlankAndEqual.test("NA", employee.getApprovalsRequired()))
                employee.setApprovalsRequired("Y");
            employee.setDeleted("N");
            employeeDAO.create(employee);
            User user = userDAO.getUserByUName(userName);
            jmsMailService.registerToMailQueue(
                    Utility.createEventMailObject(user.getId(),
                            EventEnum.ADD.toString(),
                            EventType.EMP.toString(),
                            employee.getId()));
        }
    }

    @Override
    public void update(Employee employee, String userName) {
        if (isEmployeeAlreadyPresent.test(employee.getECode())) {
            throw new ApplicationException("DUPLICATE ECODE");
        } else {
            if (StringUtils.areBothNotBlankAndEqual.test("NA", employee.getApprovalsRequired()))
                employee.setApprovalsRequired("Y");
            Employee dbEmployee = employeeDAO.getById(employee.getId());
            if (employee.getApprovalsRequired() != null) {
                dbEmployee.setApprovalsRequired(employee.getApprovalsRequired());
            }
            if (employee.getECode() != null)
                dbEmployee.setECode(employee.getECode());
            if (employee.getEmail() != null)
                dbEmployee.setEmail(employee.getEmail());
            if (employee.getFirstName() != null)
                dbEmployee.setFirstName(employee.getFirstName());
            if (employee.getLastName() != null)
                dbEmployee.setLastName(employee.getLastName());
            if (employee.getMobile() != null)
                dbEmployee.setMobile(employee.getMobile());
            employeeDAO.update(dbEmployee);
            User user = userDAO.getUserByUName(userName);
            jmsMailService.registerToMailQueue(
                    Utility.createEventMailObject(user.getId(),
                            EventEnum.UPDATE.toString(),
                            EventType.EMP.toString(),
                            employee.getId()));
        }
    }
    Predicate<String> isEmployeeAlreadyPresent = eCode -> employeeDAO.countActiveEmployee(eCode) > 0;
}
