package me.theentropyshard.crlauncher.cosmic.account;

import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.utils.json.Json;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class ItchIoAccount extends Account {
    private String itchIoApiKey;
    private ItchProfile itchProfile;

    public ItchIoAccount() {

    }

    public ItchIoAccount(String itchIoApiKey) {
        this.itchIoApiKey = itchIoApiKey;
    }

    public ItchIoAccount(String username, String itchIoApiKey) {
        super(username);

        this.itchIoApiKey = itchIoApiKey;
    }

    @Override
    public void authenticate() {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            Headers headers = Headers.of("Authorization", "Bearer " + this.itchIoApiKey);
            String json = request.asString("https://itch.io/api/1/key/me", headers);
            JsonObject userObject = Json.parse(json, JsonObject.class).getAsJsonObject("user");
            this.itchProfile = Json.parse(userObject, ItchProfile.class);
            this.setUsername(this.itchProfile.getUsername());
            this.setUniqueId(this.itchProfile.getId());
            String coverUrl = this.itchProfile.getCoverUrl();

            if (coverUrl != null) {
                Request imageRequest = new Request.Builder()
                    .url(coverUrl)
                    .build();
                try (Response response = CRLauncher.getInstance().getHttpClient().newCall(imageRequest).execute()) {
                    byte[] imageBytes = Objects.requireNonNull(response.body()).bytes();
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    Image scaledImage = bufferedImage.getScaledInstance(32, 32, BufferedImage.SCALE_FAST);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BufferedImage newImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = newImage.getGraphics();
                    graphics.drawImage(scaledImage, 0, 0, null);
                    graphics.dispose();
                    ImageIO.write(newImage, "PNG", baos);
                    this.setHeadIcon(new String(Base64.getMimeEncoder().encode(
                        baos.toByteArray()
                    ), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public String getItchIoApiKey() {
        return this.itchIoApiKey;
    }

    public ItchProfile getItchProfile() {
        return this.itchProfile;
    }
}
