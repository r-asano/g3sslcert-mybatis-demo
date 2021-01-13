package com.java.judge.controller;

//@Controller
public class JudgeController {

//	@Autowired
//	private JudgeRepository judgemapper;

	// 以下、WEB UI用のアノテーション

//	@GetMapping
//	public String crtId(Model model) {
//		model.addAttribute("items", itemService.findAll());
//		return "index";
//	}
//
//	@GetMapping("{id}")
//	public String show(@PathVariable Long id, Model model) {
//		model.addAttribute("item", itemService.findOne(id));
//		return "show";
//	}
//
//	@GetMapping("new")
//	public String newItem(@ModelAttribute("item") Item item, Model model) {
//		return "new";
//	}
//
//	@GetMapping("{id}/edit")
//	public String edit(@PathVariable Long id, @ModelAttribute("item") Item item, Model model) {
//		model.addAttribute("item", itemService.findOne(id));
//		return "edit";
//	}
//
//	@PostMapping
//	public String create(@ModelAttribute("item") @Validated Item item, BindingResult result, Model model) {
//		if(result.hasErrors()) {
//			return "new";
//		} else {
//			itemService.save(item);
//			return "redirect:/items";
//		}
//	}
//
//	@PutMapping("{id}")
//	public String update(@PathVariable Long id, @ModelAttribute("item") @Validated Item item, BindingResult result, Model model) {
//		if(result.hasErrors()) {
//			model.addAttribute("item", item);
//			return "edit";
//		} else {
//			item.setId(id);
//			itemService.update(item);
//			return "redirect:/items";
//		}
//	}
//
//	@DeleteMapping("{id}")
//	public String delete(@PathVariable Long id) {
//		itemService.delete(id);
//		return "redirect:/items";
//	}

}
