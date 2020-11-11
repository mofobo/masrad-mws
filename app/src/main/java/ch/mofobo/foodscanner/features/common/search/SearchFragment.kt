package ch.mofobo.foodscanner.features.common.search

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import ch.mofobo.foodscanner.R
import ch.mofobo.foodscanner.common.StringUtils
import ch.mofobo.foodscanner.domain.model.Lang
import ch.mofobo.foodscanner.domain.model.NutrientInfo
import ch.mofobo.foodscanner.domain.model.Nutrients
import ch.mofobo.foodscanner.domain.model.Product
import ch.mofobo.foodscanner.features.common.search.gallery.ImageGalleryAdapter
import ch.mofobo.foodscanner.features.common.search.gallery.ImageGalleryLayoutManager
import ch.mofobo.foodscanner.utils.recyclerview.RecyclerViewDividerMarginItemDecoration
import kotlinx.android.synthetic.main.fragment_search.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.reflect.full.memberProperties


class SearchFragment : DialogFragment() {

    private lateinit var navController: NavController
    private val viewModel: SearchViewModel by viewModel()

    val args: SearchFragmentArgs by navArgs()

    private lateinit var imageGalleryAdapter: ImageGalleryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(LAYOUT_ID, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = NavHostFragment.findNavController(this)
        prepareView()
        oberveViewModel()

        viewModel.searchProduct(args.barcode)
    }

    private fun navigateTo(destination: Int) {
        navController.navigate(destination)
    }

    private fun prepareView() {
        prepareAdapter()
        //nutrients_table_webview.loadUrl("file:///android_asset/nutrients_template.html")
    }

    private fun prepareAdapter() {
        image_gallery_rv.layoutManager = ImageGalleryLayoutManager(context)

        image_gallery_rv.addItemDecoration(
            RecyclerViewDividerMarginItemDecoration(
                resources.getDimension(R.dimen.image_gallery_item_margin).toInt()
            )
        )

        imageGalleryAdapter = ImageGalleryAdapter()
        image_gallery_rv.adapter = imageGalleryAdapter
        imageGalleryAdapter.setData(listOf("", "", ""))

    }

    private fun oberveViewModel() {

        viewModel.product.observe(viewLifecycleOwner, Observer {
            it?.data?.let {
                displayProduct(it)
            }
        })
    }

    private fun displayProduct(product: Product) {
        product.let {
            product.let {

                name_tv.text = it.name_translations.getTranslation(LANG, it.barcode)

                imageGalleryAdapter.setData(it.getImages("large"))

                Handler().postDelayed({ displayNutrients(it) }, 10)
            }
        }

    }

    private fun displayNutrients(product: Product) {
        var htmlTemplate = readeFileFromAssets("nutrients_template.html")

        val nutrientInfosHTML = mutableListOf<String>()
        for (property in Nutrients::class.memberProperties) {
            val nutrient = property.call(product.nutrients) as NutrientInfo?
            nutrient?.let {
                val name = nutrient.nameTranslations.getTranslation(LANG, property.name)
                val perHundred = StringUtils.trimTrailingZeroAndAddSuffix(nutrient.perHundred, " ${nutrient.unit}", "")
                val perPortion = StringUtils.trimTrailingZeroAndAddSuffix(nutrient.perPortion, " ${nutrient.unit}", "")
                val perDay = StringUtils.trimTrailingZeroAndAddSuffix(nutrient.perDay, "", "")
                val nutrientInfoHtml = String.format(NUTRIENT_MAIN_HTML_TEMPLATE, name, perHundred, perPortion, perDay)
                val nutrientInfoHtml2 = String.format(NUTRIENT_SUB_HTML_TEMPLATE, name, perHundred, perPortion, perDay)
                nutrientInfosHTML.add(nutrientInfoHtml)
                nutrientInfosHTML.add(nutrientInfoHtml2)
            }
        }
        htmlTemplate = htmlTemplate.replace("[NUTRIENTS_ITEMS]", nutrientInfosHTML.joinToString(separator = ""))
        nutrients_table_webview.loadDataWithBaseURL("", htmlTemplate, "text/html", "UTF-8", "")

    }

    private fun readeFileFromAssets(fileName: String): String {
        var reader: BufferedReader? = null
        var result = ""
        try {
            reader = BufferedReader(InputStreamReader(requireActivity().assets.open(fileName), "UTF-8"))

            var line: String?
            do {
                line = reader.readLine()
                result += line ?: ""
            } while (line != null)

        } catch (e: IOException) { /*log the exception*/
        } finally {
            reader?.let {
                try {
                    it.close()
                } catch (e: IOException) { /* log the exception */
                }
            }
        }
        return result
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    companion object {
        private const val NUTRIENT_MAIN_HTML_TEMPLATE =
            "<tr><th colspan=\"2\"><b>%1s</b></th><td style=\"text-align:right\"><b>%2s</b></td><td style=\"text-align:right\"><b>%3s</b></td><<td><b>%4s</b></td>/tr>"
        private const val NUTRIENT_SUB_HTML_TEMPLATE =
            "<tr><td class=\"blank-cell\"></td><th>%1s</th><td style=\"text-align:right\"><b>%2s</b></td><td style=\"text-align:right\"><b>%3s</b></td><td style=\"text-align:right\"><b>%4s</b></td></tr>"

        private val LANG = Lang.ENGLISCH

        private const val LAYOUT_ID = R.layout.fragment_search
    }

}