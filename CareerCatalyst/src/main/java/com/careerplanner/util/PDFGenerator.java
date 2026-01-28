package com.careerplanner.util;

import com.careerplanner.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates PDF files for resumes using Apache PDFBox.
 */
public class PDFGenerator {
    
    // Font constants
    private static final PDFont TITLE_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDFont SUBTITLE_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDFont SECTION_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDFont NORMAL_FONT = PDType1Font.HELVETICA;
    private static final PDFont ITALIC_FONT = PDType1Font.HELVETICA_OBLIQUE;
    
    // Font sizes
    private static final float TITLE_SIZE = 18;
    private static final float SUBTITLE_SIZE = 14;
    private static final float SECTION_SIZE = 12;
    private static final float NORMAL_SIZE = 10;
    private static final float SMALL_SIZE = 9;
    
    // Layout constants
    private static final float MARGIN = 50;
    private static final float LINE_SPACING = 1.5f * NORMAL_SIZE;
    
    // Colors
    private static final float[] BLUE_COLOR = {0f, 0.4f, 0.8f}; // Primary color
    private static final float[] DARK_GREY_COLOR = {0.173f, 0.243f, 0.314f}; // Secondary color
    private static final float[] GREEN_COLOR = {0.153f, 0.682f, 0.376f}; // Accent color
    
    /**
     * Generates a PDF file for a user's resume.
     *
     * @param user The user whose resume to generate
     * @param outputFile The output PDF file
     * @return true if PDF generation was successful, false otherwise
     */
    public boolean generateResumePDF(User user, File outputFile) {
        Resume resume = user.getResume();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            
            // Apply resume template style
            applyTemplateStyle(contentStream, resume.getTemplate());
            
            // Header section with name and contact info
            yPosition = drawHeader(contentStream, user, yPosition);
            
            // Summary section
            if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
                yPosition = drawSection(contentStream, "Professional Summary", yPosition);
                yPosition = drawParagraph(contentStream, resume.getSummary(), yPosition);
            }
            
            // Skills section
            if (!user.getSkills().isEmpty()) {
                yPosition = drawSection(contentStream, "Skills", yPosition);
                
                List<String> skills = new ArrayList<>();
                for (Skill skill : user.getSkills()) {
                    if (skill.isIncludeInResume()) {
                        skills.add(skill.getName() + " (" + skill.getProficiencyLevel().getDisplayName() + ")");
                    }
                }
                
                yPosition = drawBulletList(contentStream, skills, yPosition);
            }
            
            // Check if we need a new page
            if (yPosition < MARGIN + 100) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                applyTemplateStyle(contentStream, resume.getTemplate());
                yPosition = page.getMediaBox().getHeight() - MARGIN;
            }
            
            // Education section
            if (!resume.getEducationList().isEmpty()) {
                yPosition = drawSection(contentStream, "Education", yPosition);
                
                for (Resume.Education education : resume.getEducationList()) {
                    yPosition = drawSubsection(contentStream, education.getDegree(), education.getInstitution(), 
                                              education.getStartDate() + " - " + education.getEndDate(), yPosition);
                    
                    if (education.getLocation() != null && !education.getLocation().isEmpty()) {
                        yPosition = drawText(contentStream, education.getLocation(), NORMAL_FONT, NORMAL_SIZE, yPosition);
                    }
                    
                    if (education.getGpa() != null && !education.getGpa().isEmpty()) {
                        yPosition = drawText(contentStream, "GPA: " + education.getGpa(), NORMAL_FONT, NORMAL_SIZE, yPosition);
                    }
                    
                    if (education.getDescription() != null && !education.getDescription().isEmpty()) {
                        yPosition = drawParagraph(contentStream, education.getDescription(), yPosition);
                    }
                    
                    yPosition -= NORMAL_SIZE / 2; // Add some space between education entries
                }
            }
            
            // Check if we need a new page
            if (yPosition < MARGIN + 100) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                applyTemplateStyle(contentStream, resume.getTemplate());
                yPosition = page.getMediaBox().getHeight() - MARGIN;
            }
            
            // Experience section
            if (!resume.getWorkExperienceList().isEmpty()) {
                yPosition = drawSection(contentStream, "Work Experience", yPosition);
                
                for (Resume.Experience experience : resume.getWorkExperienceList()) {
                    String endDate = experience.getEndDate() != null ? experience.getEndDate() : "Present";
                    yPosition = drawSubsection(contentStream, experience.getPosition(), experience.getCompany(),
                                              experience.getStartDate() + " - " + endDate, yPosition);
                    
                    if (experience.getLocation() != null && !experience.getLocation().isEmpty()) {
                        yPosition = drawText(contentStream, experience.getLocation(), NORMAL_FONT, NORMAL_SIZE, yPosition);
                    }
                    
                    if (experience.getDescription() != null && !experience.getDescription().isEmpty()) {
                        yPosition = drawParagraph(contentStream, experience.getDescription(), yPosition);
                    }
                    
                    if (!experience.getResponsibilities().isEmpty()) {
                        List<String> responsibilities = new ArrayList<>(experience.getResponsibilities());
                        yPosition = drawBulletList(contentStream, responsibilities, yPosition);
                    }
                    
                    yPosition -= NORMAL_SIZE; // Add some space between experience entries
                    
                    // Check if we need a new page
                    if (yPosition < MARGIN + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        applyTemplateStyle(contentStream, resume.getTemplate());
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                    }
                }
            }
            
            // Projects section
            if (!resume.getProjectsList().isEmpty()) {
                yPosition = drawSection(contentStream, "Projects", yPosition);
                
                for (Resume.Project project : resume.getProjectsList()) {
                    String endDate = project.getEndDate() != null ? project.getEndDate() : "Present";
                    yPosition = drawSubsection(contentStream, project.getName(), 
                                              "Timeline: " + project.getStartDate() + " - " + endDate, null, yPosition);
                    
                    if (project.getTechnologies() != null && !project.getTechnologies().isEmpty()) {
                        yPosition = drawText(contentStream, "Technologies: " + project.getTechnologies(), 
                                           NORMAL_FONT, NORMAL_SIZE, yPosition);
                    }
                    
                    if (project.getDescription() != null && !project.getDescription().isEmpty()) {
                        yPosition = drawParagraph(contentStream, project.getDescription(), yPosition);
                    }
                    
                    if (project.getUrl() != null && !project.getUrl().isEmpty()) {
                        yPosition = drawText(contentStream, "URL: " + project.getUrl(), ITALIC_FONT, NORMAL_SIZE, yPosition);
                    }
                    
                    yPosition -= NORMAL_SIZE / 2; // Add some space between project entries
                    
                    // Check if we need a new page
                    if (yPosition < MARGIN + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        applyTemplateStyle(contentStream, resume.getTemplate());
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                    }
                }
            }
            
            // Languages section
            if (!resume.getLanguages().isEmpty()) {
                yPosition = drawSection(contentStream, "Languages", yPosition);
                
                StringBuilder languages = new StringBuilder();
                for (int i = 0; i < resume.getLanguages().size(); i++) {
                    languages.append(resume.getLanguages().get(i));
                    if (i < resume.getLanguages().size() - 1) {
                        languages.append(", ");
                    }
                }
                
                yPosition = drawText(contentStream, languages.toString(), NORMAL_FONT, NORMAL_SIZE, yPosition);
                yPosition -= NORMAL_SIZE; // Add some space
            }
            
            // References section
            if (!resume.getReferences().isEmpty()) {
                yPosition = drawSection(contentStream, "References", yPosition);
                
                for (String reference : resume.getReferences()) {
                    yPosition = drawText(contentStream, reference, NORMAL_FONT, NORMAL_SIZE, yPosition);
                    yPosition -= NORMAL_SIZE / 2;
                }
                
                yPosition -= NORMAL_SIZE / 2; // Add some space
            }
            
            // Additional info section
            if (resume.getAdditionalInfo() != null && !resume.getAdditionalInfo().isEmpty()) {
                yPosition = drawSection(contentStream, "Additional Information", yPosition);
                yPosition = drawParagraph(contentStream, resume.getAdditionalInfo(), yPosition);
            }
            
            contentStream.close();
            document.save(outputFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Applies a template style to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param template The resume template to apply
     * @throws IOException If there is an error writing to the PDF
     */
    private void applyTemplateStyle(PDPageContentStream contentStream, Resume.Template template) throws IOException {
        // Set default text color
        contentStream.setNonStrokingColor(0, 0, 0); // Black
        
        // Apply specific template styles
        switch (template) {
            case PROFESSIONAL:
                // Professional template uses blue accents
                break;
                
            case CREATIVE:
                // Creative template uses a different color scheme
                contentStream.setNonStrokingColor(DARK_GREY_COLOR[0], DARK_GREY_COLOR[1], DARK_GREY_COLOR[2]);
                break;
                
            case MINIMALIST:
                // Minimalist template uses black and white
                break;
                
            case ACADEMIC:
                // Academic template uses a serif font (already handled in font selection)
                break;
                
            case TECHNICAL:
                // Technical template emphasizes skills and technical projects
                break;
        }
    }
    
    /**
     * Draws the header section with name and contact info.
     *
     * @param contentStream The PDF content stream
     * @param user The user with contact information
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawHeader(PDPageContentStream contentStream, User user, float yPosition) throws IOException {
        // Draw name
        contentStream.setNonStrokingColor(BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2]);
        contentStream.beginText();
        contentStream.setFont(TITLE_FONT, TITLE_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(user.getFullName());
        contentStream.endText();
        
        yPosition -= TITLE_SIZE + 5;
        
        // Draw contact info
        contentStream.setNonStrokingColor(0, 0, 0); // Back to black
        contentStream.beginText();
        contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        
        StringBuilder contactInfo = new StringBuilder();
        contactInfo.append("Email: ").append(user.getEmail());
        
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            contactInfo.append(" | Phone: ").append(user.getPhone());
        }
        
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            contactInfo.append(" | Address: ").append(user.getAddress());
        }
        
        contentStream.showText(contactInfo.toString());
        contentStream.endText();
        
        yPosition -= LINE_SPACING * 2;
        
        // Draw divider line
        contentStream.setStrokingColor(BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2]);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PDRectangle.A4.getWidth() - MARGIN, yPosition);
        contentStream.stroke();
        
        return yPosition - LINE_SPACING;
    }
    
    /**
     * Draws a section header.
     *
     * @param contentStream The PDF content stream
     * @param title The section title
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawSection(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.setNonStrokingColor(BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2]);
        contentStream.beginText();
        contentStream.setFont(SECTION_FONT, SECTION_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        
        // Draw underline
        contentStream.setStrokingColor(BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, yPosition - 2);
        contentStream.lineTo(MARGIN + title.length() * (SECTION_SIZE * 0.6f), yPosition - 2);
        contentStream.stroke();
        
        contentStream.setNonStrokingColor(0, 0, 0); // Back to black
        
        return yPosition - LINE_SPACING;
    }
    
    /**
     * Draws a subsection header with title, subtitle, and date.
     *
     * @param contentStream The PDF content stream
     * @param title The subsection title
     * @param subtitle The subsection subtitle
     * @param date The subsection date
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawSubsection(PDPageContentStream contentStream, String title, String subtitle, 
                               String date, float yPosition) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float titleWidth = TITLE_FONT.getStringWidth(title) / 1000 * SUBTITLE_SIZE;
        float dateWidth = NORMAL_FONT.getStringWidth(date) / 1000 * NORMAL_SIZE;
        
        // Draw title - bold
        contentStream.beginText();
        contentStream.setFont(SUBTITLE_FONT, SUBTITLE_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        
        // Draw date on the right side
        if (date != null) {
            contentStream.beginText();
            contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
            contentStream.newLineAtOffset(pageWidth - MARGIN - dateWidth, yPosition);
            contentStream.showText(date);
            contentStream.endText();
        }
        
        yPosition -= NORMAL_SIZE + 5;
        
        // Draw subtitle - normal text
        contentStream.beginText();
        contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(subtitle);
        contentStream.endText();
        
        return yPosition - NORMAL_SIZE;
    }
    
    /**
     * Draws regular text.
     *
     * @param contentStream The PDF content stream
     * @param text The text to draw
     * @param font The font to use
     * @param fontSize The font size
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawText(PDPageContentStream contentStream, String text, PDFont font, 
                         float fontSize, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        
        return yPosition - LINE_SPACING;
    }
    
    /**
     * Draws a paragraph of text with wrapping.
     *
     * @param contentStream The PDF content stream
     * @param text The paragraph text
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawParagraph(PDPageContentStream contentStream, String text, float yPosition) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float textWidth = pageWidth - (2 * MARGIN);
        
        List<String> lines = wrapText(text, NORMAL_FONT, NORMAL_SIZE, textWidth);
        
        for (String line : lines) {
            contentStream.beginText();
            contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            
            yPosition -= LINE_SPACING;
        }
        
        return yPosition;
    }
    
    /**
     * Draws a list of bullet points.
     *
     * @param contentStream The PDF content stream
     * @param items The list items
     * @param yPosition The starting y-position
     * @return The new y-position after drawing
     * @throws IOException If there is an error writing to the PDF
     */
    private float drawBulletList(PDPageContentStream contentStream, List<String> items, float yPosition) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float textWidth = pageWidth - (2 * MARGIN + 10); // Account for bullet indent
        
        for (String item : items) {
            // Draw bullet
            contentStream.beginText();
            contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("•");
            contentStream.endText();
            
            // Wrap and draw list item text
            List<String> lines = wrapText(item, NORMAL_FONT, NORMAL_SIZE, textWidth);
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                float lineOffset = (i == 0) ? 0 : LINE_SPACING;
                
                contentStream.beginText();
                contentStream.setFont(NORMAL_FONT, NORMAL_SIZE);
                contentStream.newLineAtOffset(MARGIN + 10, yPosition - lineOffset * i);
                contentStream.showText(line);
                contentStream.endText();
            }
            
            yPosition -= LINE_SPACING * lines.size();
        }
        
        return yPosition;
    }
    
    /**
     * Wraps text to fit within a given width.
     *
     * @param text The text to wrap
     * @param font The font being used
     * @param fontSize The font size
     * @param width The maximum width
     * @return A list of wrapped lines
     * @throws IOException If there is an error in PDF text operations
     */
    private List<String> wrapText(String text, PDFont font, float fontSize, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            int lastSpace = -1;
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            
            for (String word : words) {
                if (line.length() > 0) {
                    line.append(" ");
                }
                
                line.append(word);
                float size = fontSize * font.getStringWidth(line.toString()) / 1000;
                
                if (size > width) {
                    if (lastSpace < 0) {
                        // Line is just the current word, which is too long
                        lines.add(line.toString());
                        line = new StringBuilder();
                    } else {
                        // Remove the word that caused overflow and create a new line
                        line.delete(lastSpace, line.length());
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    }
                    lastSpace = -1;
                } else {
                    lastSpace = line.length() - word.length();
                }
            }
            
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        
        return lines;
    }
}
