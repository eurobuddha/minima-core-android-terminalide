package com.eurobuddha.terminalide.terminal;

import android.app.Activity;
import android.os.Looper;
import android.widget.TextView;
import com.eurobuddha.terminalide.NodeApi;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34)
public class TerminalRenderingTest {
    private TerminalView terminal;
    private NodeApi node;
    private NodeApi.Cb callback;
    @Before public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        activity.setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        terminal = new TerminalView(activity); activity.setContentView(terminal.getMainView());
        node = mock(NodeApi.class); terminal.setNodeApi(node);
        doAnswer(i -> {callback=i.getArgument(1); return null;}).when(node).cmd(anyString(),any());
    }
    @After public void close() {terminal.onDestroy();}
    private void awaitResult() throws Exception { awaitResult("end-marker"); }
    private void awaitResult(String marker) throws Exception {
        long until=System.nanoTime()+10_000_000_000L;
        while (System.nanoTime()<until) {
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            if (terminal.exportText().contains(marker)) return;
            Thread.sleep(10);
        }
        fail("Background formatter did not complete");
    }
    private TextView lastLabel() {
        return (TextView)terminal.mOutput.getChildAt(terminal.mOutput.getChildCount()-1);
    }
    @Test public void multiMegabyteReplyDrawsSmallPreviewButExportsCompleteJson() throws Exception {
        terminal.runCMD("balance");
        JSONObject reply = new JSONObject().put("status",true).put("image","a".repeat(5*1024*1024)).put("marker","end-marker");
        callback.onResult(reply); awaitResult();
        TextView label=(TextView)terminal.mOutput.getChildAt(terminal.mOutput.getChildCount()-1);
        assertTrue(label.length()<14000);
        assertTrue(label.getText().toString().contains("Preview shortened"));
        assertTrue(terminal.exportText().contains(reply.toString(2)));
        assertTrue(terminal.exportText().contains("MINIMA  TERMINAL  IDE"));
        terminal.setSelectionMode(true);
        assertTrue(label.length()<14000);
        verify(node,times(1)).cmd(eq("balance"),any());
    }
    @Test public void clearingTerminalRendersLateResultsIntoAFreshBlockWithoutRerunningTheCommand() throws Exception {
        terminal.runCMD("balance"); terminal.clearTerminal();
        assertEquals(1,terminal.mOutput.getChildCount());
        callback.onResult(new JSONObject().put("status",true).put("marker","end-marker"));
        awaitResult();
        // banner + late echo + result: the txpowid of a slow send is never silently dropped.
        assertEquals(3,terminal.mOutput.getChildCount());
        assertTrue(terminal.exportText().contains("❯ balance  (reply arrived after"));
        verify(node,times(1)).cmd(eq("balance"),any());
        // The late reply also released its admission slot.
        for(int i=0;i<8;i++) terminal.runCMD("status");
        verify(node,times(8)).cmd(eq("status"),any());
    }
    @Test public void helpPageRendersFormattedEvenWhenItsJsonExceedsTheLineBound() throws Exception {
        StringBuilder fullhelp = new StringBuilder();
        for (int i=0; fullhelp.length()<2500; i++) fullhelp.append("send amount:1 address:0xFF.. line ").append(i).append('\n');
        JSONObject reply = new JSONObject().put("status",true).put("response", new JSONObject()
                .put("command","send").put("help","Send Minima or tokens").put("fullhelp",fullhelp.toString()));
        terminal.runCMD("help command:send"); callback.onResult(reply); awaitResult("⏱");
        String shown = lastLabel().getText().toString();
        assertFalse(shown.contains("fullhelp"));
        assertFalse(shown.contains("Preview shortened"));
        assertTrue(shown.startsWith("send  "));
        assertEquals(shown, ((TerminalLabel)lastLabel()).exportText());
    }
    @Test public void embeddedNewlinesRenderAsRealLines() throws Exception {
        terminal.runCMD("scripts");
        callback.onResult(new JSONObject().put("status",true).put("script","LET a=1\nLET b=2\tend-marker"));
        awaitResult();
        String shown = lastLabel().getText().toString();
        assertTrue(shown.contains("LET a=1\nLET b=2    end-marker"));
        assertFalse(shown.contains("\\n"));
    }
    @Test public void oversizedCommandIsRefusedBeforeAdmissionOrDispatch() {
        terminal.runCMD("a".repeat(256_001));
        verify(node,never()).cmd(anyString(),any());
        assertTrue(terminal.exportText().contains("✖ Command is 256001 chars; the node accepts at most 256000."));
        // No in-flight slot was taken: all eight admissions remain.
        for(int i=0;i<8;i++) terminal.runCMD("status");
        verify(node,times(8)).cmd(eq("status"),any());
    }
    @Test public void longCommandEchoKeepsColourAndSaysItIsShortened() {
        terminal.runCMD("txnimport data:" + "f".repeat(2000));
        TextView echo=(TextView)terminal.mOutput.getChildAt(1);
        android.text.Spanned text=(android.text.Spanned)echo.getText();
        assertTrue(text.toString().contains("Preview shortened"));
        android.text.style.ForegroundColorSpan[] spans=text.getSpans(0,1,android.text.style.ForegroundColorSpan.class);
        assertEquals(OutputFormatter.COL_CMD,spans[0].getForegroundColor());
        assertTrue(((TerminalLabel)echo).exportText().contains("f".repeat(2000)));
        callback.onError("boom " + "e".repeat(2000));
        assertTrue(lastLabel().getText().toString().contains("Preview shortened"));
    }
    @Test public void commandAdmissionStopsBeforeDispatchAndResumesAfterAReply() {
        for(int i=0;i<9;i++) terminal.runCMD("status");
        verify(node,times(8)).cmd(eq("status"),any());
        callback.onError("offline"); terminal.runCMD("status");
        verify(node,times(9)).cmd(eq("status"),any());
    }
    @Test public void responseFormattingRunsAwayFromTheMainLooper() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean formattedOnMain = new java.util.concurrent.atomic.AtomicBoolean(true);
        JSONObject reply = new JSONObject() {
            @Override public String toString(int spaces) throws org.json.JSONException {
                formattedOnMain.set(Looper.myLooper() == Looper.getMainLooper());
                return super.toString(spaces);
            }
        };
        reply.put("status",true).put("marker","end-marker");
        terminal.runCMD("status"); callback.onResult(reply); awaitResult();
        assertFalse(formattedOnMain.get());
    }
    @Test public void destructionDiscardsLateResults() throws Exception {
        terminal.runCMD("balance"); terminal.onDestroy();
        callback.onResult(new JSONObject().put("status",true).put("marker","end-marker"));
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertFalse(terminal.exportText().contains("end-marker"));
    }
}
