/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.service.common.master;

import com.iemr.mmu.data.doctor.*;
import com.iemr.mmu.data.labModule.ProcedureData;
import com.iemr.mmu.data.masterdata.anc.*;
import com.iemr.mmu.data.masterdata.nurse.FamilyMemberType;
import com.iemr.mmu.data.masterdata.pnc.NewbornHealthStatus;
import com.iemr.mmu.repo.doctor.*;
import com.iemr.mmu.repo.labModule.ProcedureRepo;
import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.repo.masterrepo.anc.*;
import com.iemr.mmu.repo.masterrepo.covid19.*;
import com.iemr.mmu.repo.masterrepo.doctor.*;
import com.iemr.mmu.repo.masterrepo.ncdCare.NCDCareTypeRepo;
import com.iemr.mmu.repo.masterrepo.nurse.FamilyMemberMasterRepo;
import com.iemr.mmu.repo.masterrepo.pnc.NewbornHealthStatusRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ANCMasterDataServiceImplTest {
    @Test
    void testGetCommonNurseMasterDataForGenopdAncNcdcarePnc_visitCategory5() {
        // Setup mocks as in the main test
        setUp();
        when(allergicReactionTypesRepo.getAllergicReactionTypes()).thenReturn(new ArrayList<>());
        when(bloodGroupsRepo.getBloodGroups()).thenReturn(new ArrayList<>());
        when(childVaccinationsRepo.getChildVaccinations()).thenReturn(new ArrayList<>());
        when(deliveryPlaceRepo.getDeliveryPlaces()).thenReturn(new ArrayList<>());
        when(deliveryTypeRepo.getDeliveryTypes()).thenReturn(new ArrayList<>());
        when(developmentProblemsRepo.getDevelopmentProblems()).thenReturn(new ArrayList<>());
        when(gestationRepo.getGestationTypes()).thenReturn(new ArrayList<>());
        when(illnessTypesRepo.getIllnessTypes(anyInt())).thenReturn(new ArrayList<>());
        when(jointTypesRepo.getJointTypes()).thenReturn(new ArrayList<>());
        when(menstrualCycleRangeRepo.getMenstrualCycleRanges(anyString())).thenReturn(new ArrayList<>());
        when(menstrualCycleStatusRepo.getMenstrualCycleStatuses(anyInt())).thenReturn(new ArrayList<>());
        when(menstrualProblemRepo.getMenstrualProblems()).thenReturn(new ArrayList<>());
        when(musculoskeletalRepo.getMusculoskeletalvalues(anyString())).thenReturn(new ArrayList<>());
        when(pregDurationRepo.getPregDurationTypes()).thenReturn(new ArrayList<>());
        when(surgeryTypesRepo.getSurgeryTypes(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(comorbidConditionRepo.getComorbidConditions(anyInt())).thenReturn(new ArrayList<>());
        when(grossMotorMilestoneRepo.getGrossMotorMilestones()).thenReturn(new ArrayList<>());
        when(fundalHeightRepo.getFundalHeights()).thenReturn(new ArrayList<>());
        when(compFeedsRepo.getCompFeeds(anyString())).thenReturn(new ArrayList<>());
        when(pregOutcomeRepo.getPregOutcomes()).thenReturn(new ArrayList<>());
        when(complicationTypesRepo.getComplicationTypes(anyString())).thenReturn(new ArrayList<>());
        when(serviceFacilityMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(new ArrayList<>());
        when(diseaseTypeRepo.getDiseaseTypes()).thenReturn(new ArrayList<>());
        when(personalHabitTypeRepo.getPersonalHabitTypeMaster(anyString())).thenReturn(new ArrayList<>());
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(new ArrayList<>());
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(optionalVaccinationsRepo.getOptionalVaccinations()).thenReturn(new ArrayList<>());
        when(newbornHealthStatusRepo.getnewBornHealthStatuses()).thenReturn(new ArrayList<>());
        when(covidSymptomsMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidContactHistoryMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidRecommnedationMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        service.getCommonNurseMasterDataForGenopdAncNcdcarePnc(5, 2, "F");
    }

    @Test
    void testGetCommonNurseMasterDataForGenopdAncNcdcarePnc_visitCategory8() {
        setUp();
        when(allergicReactionTypesRepo.getAllergicReactionTypes()).thenReturn(new ArrayList<>());
        when(bloodGroupsRepo.getBloodGroups()).thenReturn(new ArrayList<>());
        when(childVaccinationsRepo.getChildVaccinations()).thenReturn(new ArrayList<>());
        when(deliveryPlaceRepo.getDeliveryPlaces()).thenReturn(new ArrayList<>());
        when(deliveryTypeRepo.getDeliveryTypes()).thenReturn(new ArrayList<>());
        when(developmentProblemsRepo.getDevelopmentProblems()).thenReturn(new ArrayList<>());
        when(gestationRepo.getGestationTypes()).thenReturn(new ArrayList<>());
        when(illnessTypesRepo.getIllnessTypes(anyInt())).thenReturn(new ArrayList<>());
        when(jointTypesRepo.getJointTypes()).thenReturn(new ArrayList<>());
        when(menstrualCycleRangeRepo.getMenstrualCycleRanges(anyString())).thenReturn(new ArrayList<>());
        when(menstrualCycleStatusRepo.getMenstrualCycleStatuses(anyInt())).thenReturn(new ArrayList<>());
        when(menstrualProblemRepo.getMenstrualProblems()).thenReturn(new ArrayList<>());
        when(musculoskeletalRepo.getMusculoskeletalvalues(anyString())).thenReturn(new ArrayList<>());
        when(pregDurationRepo.getPregDurationTypes()).thenReturn(new ArrayList<>());
        when(surgeryTypesRepo.getSurgeryTypes(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(comorbidConditionRepo.getComorbidConditions(anyInt())).thenReturn(new ArrayList<>());
        when(grossMotorMilestoneRepo.getGrossMotorMilestones()).thenReturn(new ArrayList<>());
        when(fundalHeightRepo.getFundalHeights()).thenReturn(new ArrayList<>());
        when(compFeedsRepo.getCompFeeds(anyString())).thenReturn(new ArrayList<>());
        when(pregOutcomeRepo.getPregOutcomes()).thenReturn(new ArrayList<>());
        when(complicationTypesRepo.getComplicationTypes(anyString())).thenReturn(new ArrayList<>());
        when(serviceFacilityMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(new ArrayList<>());
        when(diseaseTypeRepo.getDiseaseTypes()).thenReturn(new ArrayList<>());
        when(personalHabitTypeRepo.getPersonalHabitTypeMaster(anyString())).thenReturn(new ArrayList<>());
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(new ArrayList<>());
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(optionalVaccinationsRepo.getOptionalVaccinations()).thenReturn(new ArrayList<>());
        when(newbornHealthStatusRepo.getnewBornHealthStatuses()).thenReturn(new ArrayList<>());
        when(covidSymptomsMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidContactHistoryMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidRecommnedationMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        service.getCommonNurseMasterDataForGenopdAncNcdcarePnc(8, 2, "F");
    }

    @Test
    void testGetCommonNurseMasterDataForGenopdAncNcdcarePnc_visitCategory10() {
        setUp();
        when(allergicReactionTypesRepo.getAllergicReactionTypes()).thenReturn(new ArrayList<>());
        when(bloodGroupsRepo.getBloodGroups()).thenReturn(new ArrayList<>());
        when(childVaccinationsRepo.getChildVaccinations()).thenReturn(new ArrayList<>());
        when(deliveryPlaceRepo.getDeliveryPlaces()).thenReturn(new ArrayList<>());
        when(deliveryTypeRepo.getDeliveryTypes()).thenReturn(new ArrayList<>());
        when(developmentProblemsRepo.getDevelopmentProblems()).thenReturn(new ArrayList<>());
        when(gestationRepo.getGestationTypes()).thenReturn(new ArrayList<>());
        when(illnessTypesRepo.getIllnessTypes(anyInt())).thenReturn(new ArrayList<>());
        when(jointTypesRepo.getJointTypes()).thenReturn(new ArrayList<>());
        when(menstrualCycleRangeRepo.getMenstrualCycleRanges(anyString())).thenReturn(new ArrayList<>());
        when(menstrualCycleStatusRepo.getMenstrualCycleStatuses(anyInt())).thenReturn(new ArrayList<>());
        when(menstrualProblemRepo.getMenstrualProblems()).thenReturn(new ArrayList<>());
        when(musculoskeletalRepo.getMusculoskeletalvalues(anyString())).thenReturn(new ArrayList<>());
        when(pregDurationRepo.getPregDurationTypes()).thenReturn(new ArrayList<>());
        when(surgeryTypesRepo.getSurgeryTypes(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(comorbidConditionRepo.getComorbidConditions(anyInt())).thenReturn(new ArrayList<>());
        when(grossMotorMilestoneRepo.getGrossMotorMilestones()).thenReturn(new ArrayList<>());
        when(fundalHeightRepo.getFundalHeights()).thenReturn(new ArrayList<>());
        when(compFeedsRepo.getCompFeeds(anyString())).thenReturn(new ArrayList<>());
        when(pregOutcomeRepo.getPregOutcomes()).thenReturn(new ArrayList<>());
        when(complicationTypesRepo.getComplicationTypes(anyString())).thenReturn(new ArrayList<>());
        when(serviceFacilityMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(new ArrayList<>());
        when(diseaseTypeRepo.getDiseaseTypes()).thenReturn(new ArrayList<>());
        when(personalHabitTypeRepo.getPersonalHabitTypeMaster(anyString())).thenReturn(new ArrayList<>());
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(new ArrayList<>());
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(optionalVaccinationsRepo.getOptionalVaccinations()).thenReturn(new ArrayList<>());
        when(newbornHealthStatusRepo.getnewBornHealthStatuses()).thenReturn(new ArrayList<>());
        when(covidSymptomsMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidContactHistoryMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidRecommnedationMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        service.getCommonNurseMasterDataForGenopdAncNcdcarePnc(10, 2, "F");
    }

    @Test
    void testGetCommonDoctorMasterDataForGenopdAncNcdcarePnc_branches() {
        // Setup mocks for all repos used in the method
        setUp();
        when(serviceMasterRepo.getAdditionalServices()).thenReturn(new ArrayList<>());
        when(instituteRepo.getInstituteDetails(anyInt())).thenReturn(new ArrayList<>());
        when(itemFormMasterRepo.getItemFormMaster()).thenReturn(new ArrayList<>());
        when(drugDoseMasterRepo.getDrugDoseMaster()).thenReturn(new ArrayList<>());
        when(drugDurationUnitMasterRepo.getDrugDurationUnitMaster()).thenReturn(new ArrayList<>());
        when(drugFrequencyMasterRepo.getDrugFrequencyMaster()).thenReturn(new ArrayList<>());
        // visitCategoryID != 7
        service.getCommonDoctorMasterDataForGenopdAncNcdcarePnc(1, 2, "F", 3, 4);
        // visitCategoryID == 7
        service.getCommonDoctorMasterDataForGenopdAncNcdcarePnc(7, 2, "F", 3, 4);
    }

    @Test
    void testAllSetters() {
        ANCMasterDataServiceImpl s = new ANCMasterDataServiceImpl();
        s.setV_DrugPrescriptionRepo(v_DrugPrescriptionRepo);
        s.setRouteOfAdminRepo(routeOfAdminRepo);
        s.setItemFormMasterRepo(itemFormMasterRepo);
        s.setOptionalVaccinationsRepo(optionalVaccinationsRepo);
        s.setProcedureRepo(procedureRepo);
        s.setNcdScreeningMasterServiceImpl(ncdScreeningMasterServiceImpl);
        s.setNcdCareTypeRepo(ncdCareTypeRepo);
        s.setNewbornHealthStatusRepo(newbornHealthStatusRepo);
        s.setAllergicReactionTypesRepo(allergicReactionTypesRepo);
        s.setBloodGroupsRepo(bloodGroupsRepo);
        s.setChildVaccinationsRepo(childVaccinationsRepo);
        s.setDeliveryPlaceRepo(deliveryPlaceRepo);
        s.setDeliveryTypeRepo(deliveryTypeRepo);
        s.setDevelopmentProblemsRepo(developmentProblemsRepo);
        s.setGestationRepo(gestationRepo);
        s.setIllnessTypesRepo(illnessTypesRepo);
        s.setJointTypesRepo(jointTypesRepo);
        s.setMenstrualCycleRangeRepo(menstrualCycleRangeRepo);
        s.setMenstrualCycleStatusRepo(menstrualCycleStatusRepo);
        s.setMenstrualProblemRepo(menstrualProblemRepo);
        s.setMusculoskeletalRepo(musculoskeletalRepo);
        s.setPregDurationRepo(pregDurationRepo);
        s.setSurgeryTypesRepo(surgeryTypesRepo);
        s.setChiefComplaintMasterRepo(chiefComplaintMasterRepo);
        s.setFamilyMemberMasterRepo(familyMemberMasterRepo);
        s.setDrugDoseMasterRepo(drugDoseMasterRepo);
        s.setDrugDurationUnitMasterRepo(drugDurationUnitMasterRepo);
        s.setDrugFrequencyMasterRepo(drugFrequencyMasterRepo);
        s.setComorbidConditionRepo(comorbidConditionRepo);
        s.setCompFeedsRepo(compFeedsRepo);
        s.setFundalHeightRepo(fundalHeightRepo);
        s.setGrossMotorMilestoneRepo(grossMotorMilestoneRepo);
        s.setServiceMasterRepo(serviceMasterRepo);
        s.setCounsellingTypeRepo(counsellingTypeRepo);
        s.setInstituteRepo(instituteRepo);
        s.setPersonalHabitTypeRepo(personalHabitTypeRepo);
        s.setPregOutcomeRepo(pregOutcomeRepo);
        s.setDiseaseTypeRepo(diseaseTypeRepo);
        s.setComplicationTypesRepo(complicationTypesRepo);
    }
    private ANCMasterDataServiceImpl service;
    // All repo and dependency mocks
    private AllergicReactionTypesRepo allergicReactionTypesRepo;
    private BloodGroupsRepo bloodGroupsRepo;
    private ChildVaccinationsRepo childVaccinationsRepo;
    private DeliveryPlaceRepo deliveryPlaceRepo;
    private DeliveryTypeRepo deliveryTypeRepo;
    private DevelopmentProblemsRepo developmentProblemsRepo;
    private GestationRepo gestationRepo;
    private IllnessTypesRepo illnessTypesRepo;
    private JointTypesRepo jointTypesRepo;
    private MenstrualCycleRangeRepo menstrualCycleRangeRepo;
    private MenstrualCycleStatusRepo menstrualCycleStatusRepo;
    private MenstrualProblemRepo menstrualProblemRepo;
    private MusculoskeletalRepo musculoskeletalRepo;
    private PregDurationRepo pregDurationRepo;
    private SurgeryTypesRepo surgeryTypesRepo;
    private ComorbidConditionRepo comorbidConditionRepo;
    private CompFeedsRepo compFeedsRepo;
    private FundalHeightRepo fundalHeightRepo;
    private GrossMotorMilestoneRepo grossMotorMilestoneRepo;
    private ServiceMasterRepo serviceMasterRepo;
    private CounsellingTypeRepo counsellingTypeRepo;
    private InstituteRepo instituteRepo;
    private PersonalHabitTypeRepo personalHabitTypeRepo;
    private PregOutcomeRepo pregOutcomeRepo;
    private DiseaseTypeRepo diseaseTypeRepo;
    private ComplicationTypesRepo complicationTypesRepo;
    private ChiefComplaintMasterRepo chiefComplaintMasterRepo;
    private FamilyMemberMasterRepo familyMemberMasterRepo;
    private DrugDoseMasterRepo drugDoseMasterRepo;
    private DrugDurationUnitMasterRepo drugDurationUnitMasterRepo;
    private DrugFrequencyMasterRepo drugFrequencyMasterRepo;
    private NewbornHealthStatusRepo newbornHealthStatusRepo;
    private NCDScreeningMasterServiceImpl ncdScreeningMasterServiceImpl;
    private NCDCareTypeRepo ncdCareTypeRepo;
    private ProcedureRepo procedureRepo;
    private CovidSymptomsMasterRepo covidSymptomsMasterRepo;
    private CovidContactHistoryMasterRepo covidContactHistoryMasterRepo;
    private CovidRecommnedationMasterRepo covidRecommnedationMasterRepo;
    private OptionalVaccinationsRepo optionalVaccinationsRepo;
    private ServiceFacilityMasterRepo serviceFacilityMasterRepo;
    private ItemFormMasterRepo itemFormMasterRepo;
    private RouteOfAdminRepo routeOfAdminRepo;
    private V_DrugPrescriptionRepo v_DrugPrescriptionRepo;
    private ItemMasterRepo itemMasterRepo;
    private MasterVanRepo masterVanRepo;

    @BeforeEach
    void setUp() {
        service = new ANCMasterDataServiceImpl();
        // Mock all dependencies
        allergicReactionTypesRepo = mock(AllergicReactionTypesRepo.class);
        bloodGroupsRepo = mock(BloodGroupsRepo.class);
        childVaccinationsRepo = mock(ChildVaccinationsRepo.class);
        deliveryPlaceRepo = mock(DeliveryPlaceRepo.class);
        deliveryTypeRepo = mock(DeliveryTypeRepo.class);
        developmentProblemsRepo = mock(DevelopmentProblemsRepo.class);
        gestationRepo = mock(GestationRepo.class);
        illnessTypesRepo = mock(IllnessTypesRepo.class);
        jointTypesRepo = mock(JointTypesRepo.class);
        menstrualCycleRangeRepo = mock(MenstrualCycleRangeRepo.class);
        menstrualCycleStatusRepo = mock(MenstrualCycleStatusRepo.class);
        menstrualProblemRepo = mock(MenstrualProblemRepo.class);
        musculoskeletalRepo = mock(MusculoskeletalRepo.class);
        pregDurationRepo = mock(PregDurationRepo.class);
        surgeryTypesRepo = mock(SurgeryTypesRepo.class);
        comorbidConditionRepo = mock(ComorbidConditionRepo.class);
        compFeedsRepo = mock(CompFeedsRepo.class);
        fundalHeightRepo = mock(FundalHeightRepo.class);
        grossMotorMilestoneRepo = mock(GrossMotorMilestoneRepo.class);
        serviceMasterRepo = mock(ServiceMasterRepo.class);
        counsellingTypeRepo = mock(CounsellingTypeRepo.class);
        instituteRepo = mock(InstituteRepo.class);
        personalHabitTypeRepo = mock(PersonalHabitTypeRepo.class);
        pregOutcomeRepo = mock(PregOutcomeRepo.class);
        diseaseTypeRepo = mock(DiseaseTypeRepo.class);
        complicationTypesRepo = mock(ComplicationTypesRepo.class);
        chiefComplaintMasterRepo = mock(ChiefComplaintMasterRepo.class);
        familyMemberMasterRepo = mock(FamilyMemberMasterRepo.class);
        drugDoseMasterRepo = mock(DrugDoseMasterRepo.class);
        drugDurationUnitMasterRepo = mock(DrugDurationUnitMasterRepo.class);
        drugFrequencyMasterRepo = mock(DrugFrequencyMasterRepo.class);
        newbornHealthStatusRepo = mock(NewbornHealthStatusRepo.class);
        ncdScreeningMasterServiceImpl = mock(NCDScreeningMasterServiceImpl.class);
        ncdCareTypeRepo = mock(NCDCareTypeRepo.class);
        procedureRepo = mock(ProcedureRepo.class);
        covidSymptomsMasterRepo = mock(CovidSymptomsMasterRepo.class);
        covidContactHistoryMasterRepo = mock(CovidContactHistoryMasterRepo.class);
        covidRecommnedationMasterRepo = mock(CovidRecommnedationMasterRepo.class);
        optionalVaccinationsRepo = mock(OptionalVaccinationsRepo.class);
        serviceFacilityMasterRepo = mock(ServiceFacilityMasterRepo.class);
        itemFormMasterRepo = mock(ItemFormMasterRepo.class);
        routeOfAdminRepo = mock(RouteOfAdminRepo.class);
        v_DrugPrescriptionRepo = mock(V_DrugPrescriptionRepo.class);
        itemMasterRepo = mock(ItemMasterRepo.class);
        masterVanRepo = mock(MasterVanRepo.class);
        // Inject all dependencies
        setField(service, "allergicReactionTypesRepo", allergicReactionTypesRepo);
        setField(service, "bloodGroupsRepo", bloodGroupsRepo);
        setField(service, "childVaccinationsRepo", childVaccinationsRepo);
        setField(service, "deliveryPlaceRepo", deliveryPlaceRepo);
        setField(service, "deliveryTypeRepo", deliveryTypeRepo);
        setField(service, "developmentProblemsRepo", developmentProblemsRepo);
        setField(service, "gestationRepo", gestationRepo);
        setField(service, "illnessTypesRepo", illnessTypesRepo);
        setField(service, "jointTypesRepo", jointTypesRepo);
        setField(service, "menstrualCycleRangeRepo", menstrualCycleRangeRepo);
        setField(service, "menstrualCycleStatusRepo", menstrualCycleStatusRepo);
        setField(service, "menstrualProblemRepo", menstrualProblemRepo);
        setField(service, "musculoskeletalRepo", musculoskeletalRepo);
        setField(service, "pregDurationRepo", pregDurationRepo);
        setField(service, "surgeryTypesRepo", surgeryTypesRepo);
        setField(service, "comorbidConditionRepo", comorbidConditionRepo);
        setField(service, "compFeedsRepo", compFeedsRepo);
        setField(service, "fundalHeightRepo", fundalHeightRepo);
        setField(service, "grossMotorMilestoneRepo", grossMotorMilestoneRepo);
        setField(service, "serviceMasterRepo", serviceMasterRepo);
        setField(service, "counsellingTypeRepo", counsellingTypeRepo);
        setField(service, "instituteRepo", instituteRepo);
        setField(service, "personalHabitTypeRepo", personalHabitTypeRepo);
        setField(service, "pregOutcomeRepo", pregOutcomeRepo);
        setField(service, "diseaseTypeRepo", diseaseTypeRepo);
        setField(service, "complicationTypesRepo", complicationTypesRepo);
        setField(service, "chiefComplaintMasterRepo", chiefComplaintMasterRepo);
        setField(service, "familyMemberMasterRepo", familyMemberMasterRepo);
        setField(service, "drugDoseMasterRepo", drugDoseMasterRepo);
        setField(service, "drugDurationUnitMasterRepo", drugDurationUnitMasterRepo);
        setField(service, "drugFrequencyMasterRepo", drugFrequencyMasterRepo);
        setField(service, "newbornHealthStatusRepo", newbornHealthStatusRepo);
        setField(service, "ncdScreeningMasterServiceImpl", ncdScreeningMasterServiceImpl);
        setField(service, "ncdCareTypeRepo", ncdCareTypeRepo);
        setField(service, "procedureRepo", procedureRepo);
        setField(service, "covidSymptomsMasterRepo", covidSymptomsMasterRepo);
        setField(service, "covidContactHistoryMasterRepo", covidContactHistoryMasterRepo);
        setField(service, "covidRecommnedationMasterRepo", covidRecommnedationMasterRepo);
        setField(service, "optionalVaccinationsRepo", optionalVaccinationsRepo);
        setField(service, "serviceFacilityMasterRepo", serviceFacilityMasterRepo);
        setField(service, "itemFormMasterRepo", itemFormMasterRepo);
        setField(service, "routeOfAdminRepo", routeOfAdminRepo);
        setField(service, "v_DrugPrescriptionRepo", v_DrugPrescriptionRepo);
        setField(service, "itemMasterRepo", itemMasterRepo);
        setField(service, "masterVanRepo", masterVanRepo);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetCommonNurseMasterDataForGenopdAncNcdcarePnc_basic() {
        // Setup all repo mocks to return empty lists or simple data
        when(allergicReactionTypesRepo.getAllergicReactionTypes()).thenReturn(new ArrayList<>());
        when(bloodGroupsRepo.getBloodGroups()).thenReturn(new ArrayList<>());
        when(childVaccinationsRepo.getChildVaccinations()).thenReturn(new ArrayList<>());
        when(deliveryPlaceRepo.getDeliveryPlaces()).thenReturn(new ArrayList<>());
        when(deliveryTypeRepo.getDeliveryTypes()).thenReturn(new ArrayList<>());
        when(developmentProblemsRepo.getDevelopmentProblems()).thenReturn(new ArrayList<>());
        when(gestationRepo.getGestationTypes()).thenReturn(new ArrayList<>());
        when(illnessTypesRepo.getIllnessTypes(anyInt())).thenReturn(new ArrayList<>());
        when(jointTypesRepo.getJointTypes()).thenReturn(new ArrayList<>());
        when(menstrualCycleRangeRepo.getMenstrualCycleRanges(anyString())).thenReturn(new ArrayList<>());
        when(menstrualCycleStatusRepo.getMenstrualCycleStatuses(anyInt())).thenReturn(new ArrayList<>());
        when(menstrualProblemRepo.getMenstrualProblems()).thenReturn(new ArrayList<>());
        when(musculoskeletalRepo.getMusculoskeletalvalues(anyString())).thenReturn(new ArrayList<>());
        when(pregDurationRepo.getPregDurationTypes()).thenReturn(new ArrayList<>());
        when(surgeryTypesRepo.getSurgeryTypes(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(comorbidConditionRepo.getComorbidConditions(anyInt())).thenReturn(new ArrayList<>());
        when(grossMotorMilestoneRepo.getGrossMotorMilestones()).thenReturn(new ArrayList<>());
        when(fundalHeightRepo.getFundalHeights()).thenReturn(new ArrayList<>());
        when(compFeedsRepo.getCompFeeds(anyString())).thenReturn(new ArrayList<>());
        when(pregOutcomeRepo.getPregOutcomes()).thenReturn(new ArrayList<>());
        when(complicationTypesRepo.getComplicationTypes(anyString())).thenReturn(new ArrayList<>());
        when(serviceFacilityMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(chiefComplaintMasterRepo.getChiefComplaintMaster()).thenReturn(new ArrayList<>());
        when(diseaseTypeRepo.getDiseaseTypes()).thenReturn(new ArrayList<>());
        when(personalHabitTypeRepo.getPersonalHabitTypeMaster(anyString())).thenReturn(new ArrayList<>());
        when(familyMemberMasterRepo.getFamilyMemberTypeMaster()).thenReturn(new ArrayList<>());
        when(procedureRepo.getProcedureMasterData(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(optionalVaccinationsRepo.getOptionalVaccinations()).thenReturn(new ArrayList<>());
        when(newbornHealthStatusRepo.getnewBornHealthStatuses()).thenReturn(new ArrayList<>());
        when(covidSymptomsMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidContactHistoryMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        when(covidRecommnedationMasterRepo.findByDeleted(false)).thenReturn(new ArrayList<>());
        try (
            org.mockito.MockedStatic<AllergicReactionTypes> allergicReactionTypesMocked = mockStatic(AllergicReactionTypes.class);
            org.mockito.MockedStatic<BloodGroups> bloodGroupsMocked = mockStatic(BloodGroups.class);
            org.mockito.MockedStatic<ChildVaccinations> childVaccinationsMocked = mockStatic(ChildVaccinations.class);
            org.mockito.MockedStatic<DeliveryPlace> deliveryPlaceMocked = mockStatic(DeliveryPlace.class);
            org.mockito.MockedStatic<DeliveryType> deliveryTypeMocked = mockStatic(DeliveryType.class);
            org.mockito.MockedStatic<DevelopmentProblems> developmentProblemsMocked = mockStatic(DevelopmentProblems.class);
            org.mockito.MockedStatic<Gestation> gestationMocked = mockStatic(Gestation.class);
            org.mockito.MockedStatic<IllnessTypes> illnessTypesMocked = mockStatic(IllnessTypes.class);
            org.mockito.MockedStatic<JointTypes> jointTypesMocked = mockStatic(JointTypes.class);
            org.mockito.MockedStatic<MenstrualCycleRange> menstrualCycleRangeMocked = mockStatic(MenstrualCycleRange.class);
            org.mockito.MockedStatic<MenstrualCycleStatus> menstrualCycleStatusMocked = mockStatic(MenstrualCycleStatus.class);
            org.mockito.MockedStatic<MenstrualProblem> menstrualProblemMocked = mockStatic(MenstrualProblem.class);
            org.mockito.MockedStatic<Musculoskeletal> musculoskeletalMocked = mockStatic(Musculoskeletal.class);
            org.mockito.MockedStatic<PregDuration> pregDurationMocked = mockStatic(PregDuration.class);
            org.mockito.MockedStatic<SurgeryTypes> surgeryTypesMocked = mockStatic(SurgeryTypes.class);
            org.mockito.MockedStatic<ComorbidCondition> comorbidConditionMocked = mockStatic(ComorbidCondition.class);
            org.mockito.MockedStatic<GrossMotorMilestone> grossMotorMilestoneMocked = mockStatic(GrossMotorMilestone.class);
            org.mockito.MockedStatic<FundalHeight> fundalHeightMocked = mockStatic(FundalHeight.class);
            org.mockito.MockedStatic<CompFeeds> compFeedsMocked = mockStatic(CompFeeds.class);
            org.mockito.MockedStatic<PregOutcome> pregOutcomeMocked = mockStatic(PregOutcome.class);
            org.mockito.MockedStatic<ComplicationTypes> complicationTypesMocked = mockStatic(ComplicationTypes.class);
            org.mockito.MockedStatic<ServiceFacilityMaster> serviceFacilityMasterMocked = mockStatic(ServiceFacilityMaster.class);
            org.mockito.MockedStatic<ChiefComplaintMaster> chiefComplaintMasterMocked = mockStatic(ChiefComplaintMaster.class);
            org.mockito.MockedStatic<DiseaseType> diseaseTypeMocked = mockStatic(DiseaseType.class);
            org.mockito.MockedStatic<PersonalHabitType> personalHabitTypeMocked = mockStatic(PersonalHabitType.class);
            org.mockito.MockedStatic<FamilyMemberType> familyMemberTypeMocked = mockStatic(FamilyMemberType.class);
            org.mockito.MockedStatic<ProcedureData> procedureDataMocked = mockStatic(ProcedureData.class);
            org.mockito.MockedStatic<OptionalVaccinations> optionalVaccinationsMocked = mockStatic(OptionalVaccinations.class);
            org.mockito.MockedStatic<NewbornHealthStatus> newbornHealthStatusMocked = mockStatic(NewbornHealthStatus.class)
        ) {
            allergicReactionTypesMocked.when(() -> AllergicReactionTypes.getAllergicReactionTypes(any())).thenReturn(new ArrayList<>());
            bloodGroupsMocked.when(() -> BloodGroups.getBloodGroups(any())).thenReturn(new ArrayList<>());
            childVaccinationsMocked.when(() -> ChildVaccinations.getChildVaccinations(any())).thenReturn(new ArrayList<>());
            deliveryPlaceMocked.when(() -> DeliveryPlace.getDeliveryPlace(any())).thenReturn(new ArrayList<>());
            deliveryTypeMocked.when(() -> DeliveryType.getDeliveryType(any())).thenReturn(new ArrayList<>());
            developmentProblemsMocked.when(() -> DevelopmentProblems.getDevelopmentProblems(any())).thenReturn(new ArrayList<>());
            gestationMocked.when(() -> Gestation.getGestations(any())).thenReturn(new ArrayList<>());
            illnessTypesMocked.when(() -> IllnessTypes.getIllnessTypes(any())).thenReturn(new ArrayList<>());
            jointTypesMocked.when(() -> JointTypes.getJointTypes(any())).thenReturn(new ArrayList<>());
            menstrualCycleRangeMocked.when(() -> MenstrualCycleRange.getMenstrualCycleRanges(any())).thenReturn(new ArrayList<>());
            menstrualCycleStatusMocked.when(() -> MenstrualCycleStatus.getMenstrualCycleStatuses(any())).thenReturn(new ArrayList<>());
            menstrualProblemMocked.when(() -> MenstrualProblem.getMenstrualProblems(any())).thenReturn(new ArrayList<>());
            musculoskeletalMocked.when(() -> Musculoskeletal.getMusculoskeletals(any())).thenReturn(new ArrayList<>());
            pregDurationMocked.when(() -> PregDuration.getPregDurationValues(any())).thenReturn(new ArrayList<>());
            surgeryTypesMocked.when(() -> SurgeryTypes.getSurgeryTypes(any())).thenReturn(new ArrayList<>());
            comorbidConditionMocked.when(() -> ComorbidCondition.getComorbidConditions(any())).thenReturn(new ArrayList<>());
            grossMotorMilestoneMocked.when(() -> GrossMotorMilestone.getGrossMotorMilestone(any())).thenReturn(new ArrayList<>());
            fundalHeightMocked.when(() -> FundalHeight.getFundalHeights(any())).thenReturn(new ArrayList<>());
            compFeedsMocked.when(() -> CompFeeds.getCompFeeds(any())).thenReturn(new ArrayList<>());
            pregOutcomeMocked.when(() -> PregOutcome.getPregOutcomes(any())).thenReturn(new ArrayList<>());
            complicationTypesMocked.when(() -> ComplicationTypes.getComplicationTypes(any(), anyInt())).thenReturn(new ArrayList<>());
            // No static method getServiceFacilityMaster exists, so skip static mocking for ServiceFacilityMaster
            chiefComplaintMasterMocked.when(() -> ChiefComplaintMaster.getChiefComplaintMasters(any())).thenReturn(new ArrayList<>());
            diseaseTypeMocked.when(() -> DiseaseType.getDiseaseTypes(any())).thenReturn(new ArrayList<>());
            personalHabitTypeMocked.when(() -> PersonalHabitType.getPersonalHabitTypeMasterData(any())).thenReturn(new ArrayList<>());
            familyMemberTypeMocked.when(() -> FamilyMemberType.getFamilyMemberTypeMasterData(any())).thenReturn(new ArrayList<>());
            procedureDataMocked.when(() -> ProcedureData.getProcedures(any())).thenReturn(new ArrayList<>());
            optionalVaccinationsMocked.when(() -> OptionalVaccinations.getOptionalVaccinations(any())).thenReturn(new ArrayList<>());
            newbornHealthStatusMocked.when(() -> NewbornHealthStatus.getNewbornHealthStatuses(any())).thenReturn(new ArrayList<>());
            String json = service.getCommonNurseMasterDataForGenopdAncNcdcarePnc(1, 2, "F");
            assertNotNull(json);
            assertTrue(json.contains("AllergicReactionTypes"));
            assertTrue(json.contains("bloodGroups"));
            assertTrue(json.contains("childVaccinations"));
            assertTrue(json.contains("deliveryPlaces"));
            assertTrue(json.contains("deliveryTypes"));
            assertTrue(json.contains("developmentProblems"));
            assertTrue(json.contains("gestation"));
            assertTrue(json.contains("illnessTypes"));
            assertTrue(json.contains("jointTypes"));
            assertTrue(json.contains("menstrualCycleLengths"));
            assertTrue(json.contains("menstrualCycleBloodFlowDuration"));
            assertTrue(json.contains("menstrualCycleStatus"));
            assertTrue(json.contains("menstrualProblem"));
            assertTrue(json.contains("musculoskeletalLateralityTypes"));
            assertTrue(json.contains("musculoskeletalAbnormalityTypes"));
            assertTrue(json.contains("pregDuration"));
            assertTrue(json.contains("surgeryTypes"));
            assertTrue(json.contains("comorbidConditions"));
            assertTrue(json.contains("grossMotorMilestones"));
            assertTrue(json.contains("fundalHeights"));
            assertTrue(json.contains("feedTypes"));
            assertTrue(json.contains("compFeedAges"));
            assertTrue(json.contains("compFeedServings"));
            assertTrue(json.contains("pregOutcomes"));
            assertTrue(json.contains("birthComplications"));
            assertTrue(json.contains("deliveryComplicationTypes"));
            assertTrue(json.contains("postpartumComplicationTypes"));
            assertTrue(json.contains("pregComplicationTypes"));
            assertTrue(json.contains("postNatalComplications"));
            assertTrue(json.contains("newBornComplications"));
            assertTrue(json.contains("chiefComplaintMaster"));
            assertTrue(json.contains("DiseaseTypes"));
            assertTrue(json.contains("tobaccoUseStatus"));
            assertTrue(json.contains("typeOfTobaccoProducts"));
            assertTrue(json.contains("alcoholUseStatus"));
            assertTrue(json.contains("typeOfAlcoholProducts"));
            assertTrue(json.contains("frequencyOfAlcoholIntake"));
            assertTrue(json.contains("quantityOfAlcoholIntake"));
            assertTrue(json.contains("familyMemberTypes"));
            assertTrue(json.contains("procedures"));
            assertTrue(json.contains("vaccineMasterData"));
        }
    }
}
