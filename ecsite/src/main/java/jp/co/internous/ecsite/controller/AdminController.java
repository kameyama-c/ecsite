package jp.co.internous.ecsite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jp.co.internous.ecsite.model.dao.GoodsRepository;
import jp.co.internous.ecsite.model.dao.UserRepository;
import jp.co.internous.ecsite.model.entity.Goods;
import jp.co.internous.ecsite.model.entity.User;
import jp.co.internous.ecsite.model.form.GoodsForm;
import jp.co.internous.ecsite.model.form.LoginForm;

@Controller
//localhost:8080/ecsite/admin/ のURLで アクセスできるよう設定
@RequestMapping("/ecsite/admin")
public class AdminController {

	// interface UserRepository・GoodsRepository.javaの読み込み
	@Autowired
	private UserRepository userRepos;
	@Autowired
	private GoodsRepository goodsRepos;

	// トップページ（adminindex.html）に 遷移するメソッッド
	@RequestMapping("/")
	public String index() {
		return "adminindex";
	}

	@PostMapping("/welcome")
	public String welcome(LoginForm form, Model m) {

		// ユーザ名とパスワードでユーザを検索
		List<User> users = userRepos.findByUserNameAndPassword(form.getUserName(), form.getPassword());

		// 検索結果が存在していれば、 isAdmin（管理者かどうか）を取得し、 管理者だった場合のみ処理
		if (users != null && users.size() > 0) {
			boolean isAdmin = users.get(0).getIsAdmin() != 0;
			if (isAdmin) {
				List<Goods> goods = goodsRepos.findAll();
				m.addAttribute("userName", users.get(0).getUserName());
				m.addAttribute("password", users.get(0).getPassword());
				m.addAttribute("goods", goods);
			}
		}

		return "welcome";
	}

	// 管理者ページに、新規商品の登録機能と、商品の削除機能を追加
	@RequestMapping("/goodsMst")
	public String goodsMst(LoginForm form, Model m) {
		m.addAttribute("userName", form.getUserName());
		m.addAttribute("password", form.getPassword());

		return "goodsmst";
	}

	// 新規商品を登録する機能 addGoodsメソッド追加
	@RequestMapping("/addGoods")
	public String addGoods(GoodsForm goodsForm,LoginForm loginForm,Model m) {
		m.addAttribute("userName",loginForm.getUserName());
		m.addAttribute("password",loginForm.getPassword());
		
		Goods goods=new Goods();
		goods.setGoodsName(goodsForm.getGoodsName());
		goods.setPrice(goodsForm.getPrice());
		goodsRepos.saveAndFlush(goods);
		
		return "forward:/ecsite/admin/welcome";
	}

	// 管理者サイトの最後の機能として、商品マスタから商品を削除する機能を作成します。
	// これまでのページ遷移による処理ではなく、ajaxを使用した方式での処理(RESTと呼ばれる)
	// deleteApiメソッド追加
	@ResponseBody
	@PostMapping("/api/deleteGoods")
	public String deleteApi(@RequestBody GoodsForm f,Model m) {
		try {
			goodsRepos.deleteById(f.getId());
		}catch(IllegalArgumentException e) {
			return "-1";
		}
		return "1";
	}
}
