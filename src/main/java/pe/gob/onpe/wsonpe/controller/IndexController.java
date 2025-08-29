package pe.gob.onpe.wsonpe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.gob.onpe.wsonpe.service.IIndexService;
import pe.gob.onpe.wsonpe.service.IndexService;

@Controller
@RequestMapping("/")
public class IndexController {

  private IIndexService indexService;

  public IndexController(IndexService indexService) {
    this.indexService = indexService;
  }

  @GetMapping("/")
  public String index(Model model) {
    return indexService.index(model);
  }

}
