package info.toyonos.hfr4droid.activity;

import info.toyonos.hfr4droid.R;
import info.toyonos.hfr4droid.core.bean.BasicElement;
import info.toyonos.hfr4droid.core.bean.Category;
import info.toyonos.hfr4droid.core.bean.Topic;
import info.toyonos.hfr4droid.core.data.HFRUrlParser;
import info.toyonos.hfr4droid.core.data.MDUrlParser;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * <p>Activity de dispatch lorsqu'une url de type forum.hardware.fr/* est catchée.</p>
 * 
 * @author ToYonos
 *
 */
public class HFR4droidDispatcher extends HFR4droidActivity
{
	private MDUrlParser urlParser = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (this.getIntent().getData() == null) finish();
		
		String url = this.getIntent().getData().toString();
		urlParser = new HFRUrlParser(getDataRetriever());
		try
		{
			if (urlParser.parseUrl(url.toString()))
			{
				BasicElement element = urlParser.getElement();
				if (element == null)
				{
					loadCats(false);
				}
				else if (element instanceof Category)
				{
					loadTopics((Category) element, urlParser.getType(), urlParser.getPage(), false);
				}
				else if (element instanceof Topic)
				{
					loadPosts((Topic) element, urlParser.getPage(), false);
				}
			}
			else
			{
				Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_LONG).show();
				finish();
			}
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), String.format(getString(R.string.error), e.getClass().getName(), e.getMessage()));
			Toast.makeText(this, getString(R.string.error_dispatching_url, e.getClass().getSimpleName(), e.getMessage()), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	public MDUrlParser getUrlParser()
	{
		return urlParser;
	}

	@Override
	protected void setTitle() {}
}