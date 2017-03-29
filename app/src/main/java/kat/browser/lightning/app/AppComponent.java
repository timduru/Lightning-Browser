package kat.browser.lightning.app;

import javax.inject.Singleton;

import kat.browser.lightning.activity.BrowserActivity;
import kat.browser.lightning.activity.ReadingActivity;
import kat.browser.lightning.activity.TabsManager;
import kat.browser.lightning.activity.ThemableBrowserActivity;
import kat.browser.lightning.activity.ThemableSettingsActivity;
import kat.browser.lightning.browser.BrowserPresenter;
import kat.browser.lightning.constant.StartPage;
import kat.browser.lightning.dialog.LightningDialogBuilder;
import kat.browser.lightning.download.LightningDownloadListener;
import kat.browser.lightning.fragment.BookmarkSettingsFragment;
import kat.browser.lightning.fragment.BookmarksFragment;
import kat.browser.lightning.fragment.DebugSettingsFragment;
import kat.browser.lightning.fragment.LightningPreferenceFragment;
import kat.browser.lightning.fragment.PrivacySettingsFragment;
import kat.browser.lightning.fragment.TabsFragment;
import kat.browser.lightning.search.SuggestionsAdapter;
import kat.browser.lightning.utils.AdBlock;
import kat.browser.lightning.utils.ProxyUtils;
import kat.browser.lightning.view.LightningView;
import kat.browser.lightning.view.LightningWebClient;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(BrowserActivity activity);

    void inject(BookmarksFragment fragment);

    void inject(BookmarkSettingsFragment fragment);

    void inject(LightningDialogBuilder builder);

    void inject(TabsFragment fragment);

    void inject(LightningView lightningView);

    void inject(ThemableBrowserActivity activity);

    void inject(LightningPreferenceFragment fragment);

    void inject(BrowserApp app);

    void inject(ProxyUtils proxyUtils);

    void inject(ReadingActivity activity);

    void inject(LightningWebClient webClient);

    void inject(ThemableSettingsActivity activity);

    void inject(AdBlock adBlock);

    void inject(LightningDownloadListener listener);

    void inject(PrivacySettingsFragment fragment);

    void inject(StartPage startPage);

    void inject(BrowserPresenter presenter);

    void inject(TabsManager manager);

    void inject(DebugSettingsFragment fragment);

    void inject(SuggestionsAdapter suggestionsAdapter);

}
