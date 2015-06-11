package site.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import site.identity.Identity;
import site.identity.IdentityUtils;
import site.racinggame.RaceInfo;
import site.racinggame.RaceResult;
import site.racinggame.Racecar;
import site.racinggame.RacingGame;
import site.racinggame.RacingGameUtils;
import site.racinggame.Upgrade;
import site.racinggame.UserRacecar;

@Controller
@Scope("session")
public class RacingGameController {
	Identity identity=null;
	RacingGame racingGame=null;
	
	@RequestMapping("/racingparent")
	public ModelAndView showParentFrame(
			@RequestParam(value = "identifier", required = true, defaultValue="0") Long identifier) {
		
		ModelAndView mv = new ModelAndView("racingParentFrame");
		identity=IdentityUtils.getIdentityByIdentifier(identifier);
		racingGame=RacingGameUtils.getRacingGameObject(identity.getRacingGameIdentifier(), identity.getIdentifier());
		
		return mv;
	}
	
	@RequestMapping("/raceMoneyFrame")
	public ModelAndView showMoneyFrame(){
		ModelAndView mv = new ModelAndView("raceMoneyFrame");
		mv.addObject("availableCash", racingGame.getAvailableCash());
		mv.addObject("racingClass", racingGame.getRacingClass().charAt(0));
		return mv;
	}
	
	@RequestMapping("/leftRacingFrame")
	public ModelAndView showLeftRacingFrame(){
		ModelAndView mv = new ModelAndView("leftRacingFrame");
		return mv;
	}
	
	@RequestMapping("/garageFrame")
	public ModelAndView showGarageFrame(){
		ModelAndView mv = new ModelAndView("garageFrame");
		mv.addObject("selectedCarID", racingGame.getCarID());
		mv.addObject("racingGame", racingGame);
		//mv.addObject("carList", racingGame.getCarList());
		
		return mv;
	}
	
	@RequestMapping("/garageDisplay")
	public ModelAndView showGarageDisplay(
			@RequestParam(value = "selectedCarID", required = true, defaultValue="0") Integer selectedCarID){
		ModelAndView mv = new ModelAndView("garageDisplay");
		racingGame.setCarID(selectedCarID);
		UserRacecar selectedCar=null;
		for (UserRacecar car : racingGame.getCarList()){
			if (car.getCarID()==selectedCarID){
				selectedCar=car;
				break;
			}
		}
		racingGame.setSelectedCar(selectedCar);
		mv.addObject("raceCar", selectedCar);
		
		return mv;
	}
	
	@RequestMapping("/openRacingStore")
	public ModelAndView openStore(){
		ModelAndView mv = new ModelAndView("openRacingStore");
		return mv;
	}
	
	@RequestMapping("/racingStoreFrame")
	public ModelAndView showStoreFrame(){
		ModelAndView mv = new ModelAndView("racingStoreFrame");
		List<Racecar> carOptions = RacingGameUtils.getAvailableCarsToPurchase(racingGame.getRacingClass(), racingGame.getRacingIdentifier());
		mv.addObject("carOptions", carOptions);
		return mv;
	}
	
	@RequestMapping("/carDisplayFrame")
	public ModelAndView showCarDisplay(
			@RequestParam(value = "carID", required = true, defaultValue="0") Integer carID){
		ModelAndView mv;
		if (carID==0){
			mv = new ModelAndView("unselectedDisplayFrame");
			mv.addObject("availableCash", racingGame.getAvailableCash());
		}else {
			mv = new ModelAndView("carDisplayFrame");
			Racecar car = RacingGameUtils.getRacecarByID(carID);
			mv.addObject("raceCar", car);
			mv.addObject("availableCash", racingGame.getAvailableCash());
		}
		return mv;
	}
	
	@RequestMapping("/purchaseCar")
	public ModelAndView purchaseCar(
			@RequestParam(value = "carID", required = true) Integer carID){
		ModelAndView mv = new ModelAndView("purchaseCar");
		Racecar raceCar = RacingGameUtils.getRacecarByID(carID);
		racingGame.setAvailableCash(racingGame.getAvailableCash()-raceCar.getPrice());
		racingGame.addNewCar(raceCar);
		Long userCarID=RacingGameUtils.saveNewCar(racingGame.getRacingIdentifier(), carID);
		racingGame.getCarList().get(racingGame.getCarList().size()-1).setUserRacecarID(userCarID);
		RacingGameUtils.updateRacingGame(racingGame.getRacingIdentifier(), racingGame.getAvailableCash(), racingGame.getRacingClass());
		return mv;
	}
	
	@RequestMapping("/openUpgradeStore")
	public ModelAndView openUpgradeStore(){
		ModelAndView mv = new ModelAndView("openUpgradeStore");
		return mv;
	}
	
	@RequestMapping("/upgradeStoreFrame")
	public ModelAndView showUpgradeStore(){
		ModelAndView mv = new ModelAndView("upgradeStoreFrame");
		List<Upgrade> upgradeOptions = RacingGameUtils.getUpgradesByClass(racingGame.getSelectedCar().getRacingClass());
		mv.addObject("upgradeOptions", upgradeOptions);
		return mv;
	}
	
	@RequestMapping("/upgradeDisplayFrame")
	public ModelAndView showUpgradeDisplay(
			@RequestParam(value = "upgradeID", required = true, defaultValue="0") Integer upgradeID){
		ModelAndView mv;
		if (upgradeID==0){
			mv = new ModelAndView("unselectedDisplayFrame");
			mv.addObject("availableCash", racingGame.getAvailableCash());
		} else {
			Upgrade upgrade = RacingGameUtils.getUpgradeByID(upgradeID);
			Double price = upgrade.getPrice();
			int numPurchases=0;
			for (Upgrade owned : racingGame.getSelectedCar().getUpgradeList()){
				if (owned.getUpgradeID()==upgradeID){
					price=price*2;
					numPurchases++;
				}
			}
			if (numPurchases>=5){
				upgrade.setPrice(null);
				mv = new ModelAndView("upgradeDisplayFrame");//TODO display another frame
			} else {
				upgrade.setPrice(price);
				mv = new ModelAndView("upgradeDisplayFrame");
			}
			mv.addObject("raceCar", racingGame.getSelectedCar());
			mv.addObject("upgrade", upgrade);
			mv.addObject("availableCash", racingGame.getAvailableCash());
		}
		
		return mv;
	}
	
	@RequestMapping("/purchaseUpgrade")
	public ModelAndView purchaseUpgrade(
			@ModelAttribute("upgrade") Upgrade upgrade){
		ModelAndView mv = new ModelAndView("purchaseCar");
		racingGame.setAvailableCash(racingGame.getAvailableCash()-upgrade.getPrice());
		racingGame.getSelectedCar().addUpgrade(upgrade);
		RacingGameUtils.addNewUpgrade(racingGame.getSelectedCar(), upgrade);
		mv.addObject("selectedCar", racingGame.getSelectedCar());
		RacingGameUtils.updateRacingGame(racingGame.getRacingIdentifier(), racingGame.getAvailableCash(), racingGame.getRacingClass());
		return mv;
	}
	
	@RequestMapping("/raceFrame")
	public ModelAndView openRaceFrame(){
		ModelAndView mv = new ModelAndView("raceFrame");
		RaceInfo raceInfo = new RaceInfo();
		List<String> availableClasses = new ArrayList<String>();
		LinkedHashMap<String, Integer> feeMap = new LinkedHashMap<String, Integer>();
		feeMap.put("E", 0);
		feeMap.put("D", 100);
		feeMap.put("C", 500);
		feeMap.put("B", 1500);
		feeMap.put("A", 3000);
		feeMap.put("S", 5000);
		availableClasses.add("E");
		if (!"E".equals(racingGame.getRacingClass())){
			availableClasses.add("D");
		}
		if (Arrays.asList("C", "B", "A", "S", "SS").contains(racingGame.getRacingClass())){
			availableClasses.add("C");
		}
		if (Arrays.asList("B", "A", "S", "SS").contains(racingGame.getRacingClass())){
			availableClasses.add("B");	
		}
		if (Arrays.asList("A", "S", "SS").contains(racingGame.getRacingClass())){
			availableClasses.add("A");
		}
		if (Arrays.asList("S", "SS").contains(racingGame.getRacingClass())){
			availableClasses.add("S");
		}
		List<String> availableCourses = new ArrayList<String>();
		availableCourses.add("normal");
		availableCourses.add("CRAZY");
		List<String> availableTypes = new ArrayList<String>();
		availableTypes.add("user");
		availableTypes.add("spectate");
		mv.addObject("racingGame", racingGame);
		mv.addObject("raceInfo", raceInfo);
		mv.addObject("availableClasses", availableClasses);
		mv.addObject("availableCourses", availableCourses);
		mv.addObject("availableTypes", availableTypes);
		mv.addObject("feeMap", feeMap);
		
		return mv;
	}
	
	@RequestMapping("/startRace")
	public ModelAndView openUserRaceFrame(
			@ModelAttribute("raceInfo") RaceInfo raceInfo){
		ModelAndView mv; 
		if (raceInfo==null){
			raceInfo=new RaceInfo();
		}
		if (raceInfo.getRaceType()==null){
			raceInfo.setRaceType("spectate");
		}
		if (raceInfo.getRaceType().equals("user")){
			mv = new ModelAndView("userRaceFrame");
		} else {
			mv = new ModelAndView("spectateRaceFrame");
		}
		List<Racecar> opponents = RacingGameUtils.getRandomOpponentsByClass(raceInfo.getRacingClass());

		raceInfo.setLapDistance(RacingGameUtils.getLapDistanceByClass(raceInfo.getRacingClass()));
		raceInfo.setNoLaps(RacingGameUtils.getNumberOfLaps());
		mv.addObject("racecar", racingGame.getSelectedCar());
		int i=0;
		if (raceInfo.getRaceType().equals("user")){
			i++;//start with racer2
		}
		for (Racecar car : opponents){
			i++;
			mv.addObject("racer"+i, car);
		}
		RaceResult raceResult = new RaceResult();
		mv.addObject("raceInfo", raceInfo);
		mv.addObject("raceResult", raceResult);
		return mv;
	}	
	
	@RequestMapping("/racingResults")
	public ModelAndView racingResults(
			@ModelAttribute("userForm") RaceResult raceResult){
		ModelAndView mv = new ModelAndView("racingResults");
		Integer finish=6;
		String newClass=null;
		if ("car1".equals(raceResult.getPlace1())){
			finish=1;
			if (raceResult.getRacingClass().equals(racingGame.getRacingClass())&&!"SS".equals(racingGame.getRacingClass())){
				if ("E".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("D");
					racingGame.setSelectedClass("D");
				} else if ("D".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("C");
					racingGame.setSelectedClass("C");
				} else if ("C".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("B");
					racingGame.setSelectedClass("B");
				}  else if ("B".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("A");
					racingGame.setSelectedClass("A");
				}  else if ("A".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("S");
					racingGame.setSelectedClass("S");
				}  else if ("S".equals(racingGame.getRacingClass())){
					racingGame.setRacingClass("SS");
				}
				newClass=racingGame.getRacingClass();
			}
			racingGame.setAvailableCash(racingGame.getAvailableCash()+RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 1));
		} else if ("car1".equals(raceResult.getPlace2())){
			finish=2;
			racingGame.setAvailableCash(racingGame.getAvailableCash()+RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 2));
		} else if ("car1".equals(raceResult.getPlace3())){
			finish=3;
			racingGame.setAvailableCash(racingGame.getAvailableCash()+RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 3));
		}
		mv.addObject("raceResult", raceResult);
		mv.addObject("finish", finish);
		mv.addObject("newClass", newClass);
		RacingGameUtils.updateRacingGame(racingGame.getRacingIdentifier(), racingGame.getAvailableCash(), racingGame.getRacingClass());
		return mv;
	}
	
}
