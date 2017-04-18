package site.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import site.view.ResultMessage;

@Controller
public class RacingGameController {
	@Autowired
	Identity identity;
	
	RacingGame racingGame=null;
	
	@RequestMapping(value="/getRacingGame", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getRacingGame(){
		if (identity.getRacingGame()==null){
			identity.setRacingGame(RacingGameUtils.getRacingGameObject(identity.getRacingGameIdentifier(), identity.getIdentifier()));
			identity.getRacingGame().setPurchaseableCars(RacingGameUtils.getAllAvailableCarsToPurchase(identity.getRacingGame()));
		}
		return new ResponseEntity<>(identity.getRacingGame(), HttpStatus.OK);
	}
	
	@RequestMapping("/racingparent")
	public ModelAndView showParentFrame(
			@RequestParam(value = "identifier") Long identifier, 
			@RequestParam(value = "delete", defaultValue="false") boolean bDelete) {
		if (identifier==null){
			return new ModelAndView("timeout");
		}
		if (bDelete){
			if (identity!=null){
				IdentityUtils.deleteRacingGame(identity.getIdentifier());
				identity.setRacingGameIdentifier(null);
			}
		} else if (identity==null||identity.getIdentifier()!=identifier){
			identity=IdentityUtils.getIdentityByIdentifier(identifier);
			racingGame=null;
		}
		ModelAndView mv = new ModelAndView("racingParentFrame");
		if (racingGame==null){
			racingGame=RacingGameUtils.getRacingGameObject(identity.getRacingGameIdentifier(), identity.getIdentifier());
		}
		
		return mv;
	}
	
	@RequestMapping("/raceMoneyFrame")
	public ModelAndView showMoneyFrame(){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("raceMoneyFrame");
		mv.addObject("availableCash", RacingGameUtils.formatMoney(racingGame.getAvailableCash()));
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
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("garageFrame");
		mv.addObject("selectedCarID", racingGame.getSelectedCar().getCarID());
		mv.addObject("racingGame", racingGame);
		//mv.addObject("carList", racingGame.getCarList());
		
		return mv;
	}
	
	@RequestMapping("/garageDisplay")
	public ModelAndView showGarageDisplay(
			@RequestParam(value = "selectedCarID", required = true, defaultValue="0") Integer selectedCarID){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("garageDisplay");
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
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("openRacingStore");
		return mv;
	}
	
	@RequestMapping("/racingStoreFrame")
	public ModelAndView showStoreFrame(){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("racingStoreFrame");
		List<Racecar> carOptions = RacingGameUtils.getAvailableCarsToPurchase(racingGame.getRacingClass(), racingGame.getRacingIdentifier());
		if (racingGame.getRacingIdentifier()==null||racingGame.getRacingIdentifier()<=0){
			for (UserRacecar usercar: racingGame.getCarList()){
				Iterator<Racecar> it = carOptions.iterator();
				while (it.hasNext()){
					Racecar car = it.next();
					if (car.getCarID()==usercar.getCarID()){
						it.remove();
						break;
					}
				}
			}
		}
		mv.addObject("carOptions", carOptions);
		return mv;
	}
	
	@RequestMapping("/carDisplayFrame")
	public ModelAndView showCarDisplay(
			@RequestParam(value = "carID", required = true, defaultValue="0") Integer carID){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv;
		if (carID==0){
			mv = new ModelAndView("unselectedDisplayFrame");
			mv.addObject("availableCash", RacingGameUtils.formatMoney(racingGame.getAvailableCash()));
		}else {
			mv = new ModelAndView("carDisplayFrame");
			Racecar car = RacingGameUtils.getRacecarByID(carID);
			mv.addObject("raceCar", car);
			mv.addObject("availableCash", racingGame.getAvailableCash());
		}
		return mv;
	}
	
	@RequestMapping("/purchaseCar")
	@ResponseBody
	public ResponseEntity<?> purchaseCar(@RequestParam(value = "carID", required = true) Integer carID){
		Racecar raceCar = RacingGameUtils.getRacecarByID(carID);
		if (identity.getRacingGame().getAvailableCash().compareTo(raceCar.getPrice())<0){
			ResultMessage result = new ResultMessage();
			result.setError(true);
			result.setResultMessage("Not Enough Money");
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		RacingGameUtils.purchaseCar(identity.getRacingGame(), raceCar);
		return new ResponseEntity<>(identity.getRacingGame(), HttpStatus.OK);
	}
	
	@RequestMapping("/openUpgradeStore")
	public ModelAndView openUpgradeStore(){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("openUpgradeStore");
		return mv;
	}
	
	@RequestMapping("/upgradeStoreFrame")
	public ModelAndView showUpgradeStore(){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("upgradeStoreFrame");
		List<Upgrade> upgradeOptions = RacingGameUtils.getUpgradesByClass(racingGame.getSelectedCar().getRacingClass());
		mv.addObject("upgradeOptions", upgradeOptions);
		return mv;
	}
	
	@RequestMapping(value="getUpgrades", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getUpgrades(@RequestParam String racingClass){
		List<Upgrade> upgradeOptions = RacingGameUtils.getUpgradesByClass(racingClass);
		for (Upgrade existing : identity.getRacingGame().getSelectedCar().getUpgradeList()){
			for (Upgrade option : upgradeOptions){
				if (existing.getUpgradeID()==option.getUpgradeID()){
					option.setPrice(option.getPrice().add(existing.getPrice()));
				}
			}
		}
		return new ResponseEntity<>(upgradeOptions, HttpStatus.OK);
	}
	
	@RequestMapping(value="purchaseUpgrade", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> purchaseUpgrade(@RequestParam Integer upgradeID){
		Upgrade upgrade = RacingGameUtils.getUpgradeByID(upgradeID);
		BigDecimal price = upgrade.getPrice();
		for (Upgrade existing : identity.getRacingGame().getSelectedCar().getUpgradeList()){
			if (existing.getUpgradeID()==upgradeID){
				price.add(upgrade.getPrice());
			}
		}
		if (identity.getRacingGame().getAvailableCash().compareTo(price)<0){
			ResultMessage result = new ResultMessage();
			result.setError(true);
			result.setResultMessage("Not Enough Money");
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		identity.getRacingGame().setAvailableCash(identity.getRacingGame().getAvailableCash().subtract(price));
		identity.getRacingGame().getSelectedCar().addUpgrade(upgrade);
		if (identity.getRacingGame().getRacingIdentifier()!=null&&identity.getRacingGame().getRacingIdentifier()>0){
			RacingGameUtils.addNewUpgrade(identity.getRacingGame().getSelectedCar(), upgrade);
			//TODO save cash
		}
		return new ResponseEntity<>(identity.getRacingGame().getSelectedCar(), HttpStatus.OK);
	}
	
	@RequestMapping("/upgradeDisplayFrame")
	public ModelAndView showUpgradeDisplay(
			@RequestParam(value = "upgradeID", required = true, defaultValue="0") Integer upgradeID){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv;
		if (upgradeID==0){
			mv = new ModelAndView("unselectedDisplayFrame");
			mv.addObject("availableCash", RacingGameUtils.formatMoney(racingGame.getAvailableCash()));
		} else {
			Upgrade upgrade = RacingGameUtils.getUpgradeByID(upgradeID);
			BigDecimal price = upgrade.getPrice();
			int numPurchases=0;
			for (Upgrade owned : racingGame.getSelectedCar().getUpgradeList()){
				if (owned.getUpgradeID()==upgradeID){
					price=price.multiply(new BigDecimal(2));
					numPurchases++;
				}
			}
			if (numPurchases>=5){
				upgrade.setPrice(null);
				mv = new ModelAndView("maxUpgradeDisplay");
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
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("purchaseCar");
		racingGame.setAvailableCash(racingGame.getAvailableCash().subtract(upgrade.getPrice()));
		racingGame.getSelectedCar().addUpgrade(upgrade);
		if (racingGame.getRacingIdentifier()!=null&&racingGame.getRacingIdentifier()>0){
			RacingGameUtils.addNewUpgrade(racingGame.getSelectedCar(), upgrade);
			//RacingGameUtils.updateRacingGame(racingGame.getRacingIdentifier(), racingGame.getAvailableCash(), racingGame.getRacingClass(), racingGame.getSelectedCar().getCarID());
		}
		mv.addObject("carModel", racingGame.getSelectedCar().getModel());
		return mv;
	}
	
	@RequestMapping("/raceFrame")
	public ModelAndView openRaceFrame(){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("raceFrame");
		RaceInfo raceInfo = new RaceInfo();
		List<String> availableClasses = new ArrayList<String>();
		LinkedHashMap<String, Integer> feeMap = RacingGameUtils.getFeeMap();
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
		availableTypes.add("spectate");
		availableTypes.add("user");
		mv.addObject("racingGame", racingGame);
		mv.addObject("raceInfo", raceInfo);
		mv.addObject("availableClasses", availableClasses);
		mv.addObject("availableCourses", availableCourses);
		mv.addObject("availableTypes", availableTypes);
		mv.addObject("feeMap", feeMap);
		
		return mv;
	}
	
	@RequestMapping("/startRace")
	@ResponseBody
	public ResponseEntity<?> openUserRaceFrame(@RequestParam String racingClass, @RequestParam String raceType, @RequestParam Integer carID){
		identity.getRacingGame().setSelectedClass(racingClass);
		if (carID!=identity.getRacingGame().getSelectedCar().getCarID()){
			for (UserRacecar car : identity.getRacingGame().getCarList()){
				if (car.getCarID()==carID){
					identity.getRacingGame().setSelectedCar(car);
					break;
				}
			}
		}
		RaceInfo raceInfo = new RaceInfo();
		raceInfo.setRaceType(raceType);
		raceInfo.setRacingClass(racingClass);
		Set<Racecar> opponents;
		if (raceType.equals("user")){
			identity.getRacingGame().setSelectedMode("user");
			LinkedHashMap<String, Integer> feeMap = RacingGameUtils.getFeeMap();
			identity.getRacingGame().setAvailableCash(identity.getRacingGame().getAvailableCash().subtract(new BigDecimal(feeMap.get(racingClass))));
			opponents = new HashSet<Racecar>(RacingGameUtils.getRandomOpponentsByClass(racingClass));
			raceInfo.setRacer1(identity.getRacingGame().getSelectedCar());
		} else {
			identity.getRacingGame().setSelectedMode("spectate");
			opponents = new HashSet<Racecar>(RacingGameUtils.getSpectateCarsByCarID(carID));
		}
		raceInfo.setLapDistance(RacingGameUtils.getLapDistanceByClass(racingClass));
		raceInfo.setNoLaps(RacingGameUtils.getNumberOfLapsByClass(racingClass));
		for (Racecar car : opponents){
			raceInfo.addRacer(car);
		}
		return new ResponseEntity<>(raceInfo, HttpStatus.OK);
	}
	
	@RequestMapping("/racingResults")
	public ModelAndView racingResults(
			@ModelAttribute("raceResult") RaceResult raceResult){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("racingResults");
		Integer finish=6;
		String newClass=null;
		if ("user".equals(raceResult.getPlace1())){
			raceResult.setPlace1(identity.getUsername());
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
			racingGame.setAvailableCash(racingGame.getAvailableCash().add(RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 1)));
		} else if ("user".equals(raceResult.getPlace2())){
			raceResult.setPlace2(identity.getUsername());
			finish=2;
			racingGame.setAvailableCash(racingGame.getAvailableCash().add(RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 2)));
		} else if ("user".equals(raceResult.getPlace3())){
			raceResult.setPlace3(identity.getUsername());
			finish=3;
			racingGame.setAvailableCash(racingGame.getAvailableCash().add(RacingGameUtils.getPurseByClass(raceResult.getRacingClass(), 3)));
		}
		raceResult.setPlace1Time(raceResult.getPlace1Time().setScale(2, RoundingMode.HALF_UP));
		raceResult.setPlace2Time(raceResult.getPlace2Time().setScale(2, RoundingMode.HALF_UP));
		raceResult.setPlace3Time(raceResult.getPlace3Time().setScale(2, RoundingMode.HALF_UP));
		mv.addObject("finish", finish);
		mv.addObject("newClass", newClass);
		if (racingGame.getRacingIdentifier()!=null&&racingGame.getRacingIdentifier()>0){
			//RacingGameUtils.updateRacingGame(racingGame.getRacingIdentifier(), racingGame.getAvailableCash(), racingGame.getRacingClass(), racingGame.getSelectedCar().getCarID());
		}
		return mv;
	}
	
	@RequestMapping("/spectateResults")
	public ModelAndView spectateResults(
			@ModelAttribute("raceResult") RaceResult raceResult){
		if (racingGame==null){
			return new ModelAndView("timeout");
		}
		ModelAndView mv = new ModelAndView("spectateResults");
		raceResult.setPlace1Time(raceResult.getPlace1Time().setScale(2, RoundingMode.HALF_UP));
		raceResult.setPlace2Time(raceResult.getPlace2Time().setScale(2, RoundingMode.HALF_UP));
		raceResult.setPlace3Time(raceResult.getPlace3Time().setScale(2, RoundingMode.HALF_UP));
		mv.addObject("raceResult", raceResult);
		return mv;
	}
	
	@RequestMapping(value = "/saveracinggame")
	//@ResponseStatus(value = HttpStatus.OK)
	public ModelAndView saveNewUser(@RequestParam(value = "identifier") Long identifier,
			@RequestParam(value = "sError", required=false) String sError){
		if (racingGame!=null){
			RacingGameUtils.insertRacingGameInfo(racingGame, identifier);
		}
		ModelAndView mv = new ModelAndView("newUserRedirect");
		mv.addObject("identifier", identifier);
		mv.addObject("type", "done");
		//mv.addObject("username", identity.getUsername());
		mv.addObject("sError", sError);
		return mv;
	}
	
}
