package cr.ac.una.andersonRymichaelS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class ArticleFragment : Fragment() {

    private lateinit var webView: WebView
    private var articleUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article, container, false)
        webView = view.findViewById(R.id.webView)

        // Obtener el URL del artículo de Wikipedia desde los argumentos
        articleUrl = arguments?.getString(ARG_ARTICLE_URL)
        articleUrl?.let { loadArticle(it) }

        return view
    }

    private fun loadArticle(url: String) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Permitir que todas las URLs se carguen en este WebView
                return false
            }
        }
        webView.loadUrl(url)
    }

    companion object {
        private const val ARG_ARTICLE_URL = "article_url"

        fun newInstance(articleUrl: String): ArticleFragment {
            val fragment = ArticleFragment()
            val args = Bundle()
            args.putString(ARG_ARTICLE_URL, articleUrl)
            fragment.arguments = args
            return fragment
        }
    }
}
