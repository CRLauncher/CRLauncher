package me.theentropyshard.crlauncher.gui.view.crmm.modview;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.Member;
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.gui.utils.GifIcon;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.gui.view.crmm.ModNoIcon;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.ImageUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CreatorsCard extends Card {
    public CreatorsCard(Project project) {
        this.setLayout(new BorderLayout());

        LanguageSection section = CRLauncher.getInstance().getLanguage().getSection("gui.searchCRMMModsDialog.modViewDialog.sideView.creatorsCard");

        JLabel creatorsLabel = new JLabel("<html><b>" + section.getString("title") + "</b><html>");
        creatorsLabel.setBorder(new EmptyBorder(-5, 0, 10, 0));
        creatorsLabel.setFont(creatorsLabel.getFont().deriveFont(16.0f));
        this.add(creatorsLabel, BorderLayout.NORTH);

        JPanel membersPanel = new JPanel();
        membersPanel.setOpaque(false);

        int count = 0;

        for (Member member : project.getMembers()) {
            membersPanel.add(new CreatorCard(member.getAvatar(), member.getUserName(), member.getRole()));

            count++;
        }

        membersPanel.setLayout(new GridLayout(count, 1));

        this.add(membersPanel, BorderLayout.CENTER);
    }

    private static final class CreatorCard extends Card {
        public CreatorCard(String avatarUrl, String name, String role) {
            this.setLayout(new BorderLayout());

            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(32, 32));
            this.add(avatarLabel, BorderLayout.WEST);

            this.fetchAvatar(avatarUrl, name, avatarLabel);

            JPanel nameRolePanel = new JPanel(new GridLayout(2, 1));
            nameRolePanel.setBorder(new EmptyBorder(0, 10, 0, 0));
            nameRolePanel.setOpaque(false);
            this.add(nameRolePanel, BorderLayout.CENTER);

            JLabel nameLabel = new JLabel("<html><b>" + name + "</b></html>");
            nameLabel.setFont(nameLabel.getFont().deriveFont(14.0f));
            nameRolePanel.add(nameLabel);

            JLabel roleLabel = new JLabel(role);
            nameRolePanel.add(roleLabel);
        }

        private void fetchAvatar(String avatarUrl, String userName, JLabel avatarLabel) {
            new Worker<Icon, Void>("fetching icon for user " + userName) {
                @Override
                protected Icon work() throws Exception {
                    OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient();

                    if (avatarUrl == null) {
                        return ModNoIcon.getInstance();
                    }

                    Request request = new Request.Builder()
                        .url(avatarUrl)
                        .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (avatarUrl.endsWith(".gif")) {
                            return new GifIcon(Objects.requireNonNull(response.body()).byteStream(), 32, 32, CreatorCard.this);
                        }

                        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(response.body()).byteStream());
                        BufferedImage scaledImage = ImageUtils.toBufferedImage(bufferedImage.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH));

                        BufferedImage clippedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);

                        Graphics2D g2d = clippedImage.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                        TexturePaint paint = new TexturePaint(scaledImage, new Rectangle(32, 32));
                        g2d.setPaint(paint);
                        g2d.fill(new RoundRectangle2D.Double(0, 0, 32, 32, 10, 10));

                        g2d.dispose();

                        return new ImageIcon(clippedImage);
                    }
                }

                @Override
                protected void done() {
                    Icon icon = null;

                    try {
                        icon = this.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.error(e);
                    }

                    if (icon == null) {
                        icon = ModNoIcon.getInstance();
                    }

                    avatarLabel.setIcon(icon);
                }
            }.execute();
        }
    }
}
