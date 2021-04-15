using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Dynamsoft;
using Dynamsoft.DBR;
using Dynamsoft.DLR;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace BarcodeAndLabelRecognition
{    
    public partial class Form1 : Form
    {
        private BarcodeReader reader = null;
        private LabelRecognition labelRecognition = null;
        public Form1()
        {
            InitializeComponent();
            reader = new BarcodeReader("t0068MgAAAKDLOibg4+wFsMoX3cusNQ6/0s9n1XgfD2KLr+Jws31Stp/iCiBs9zrQSB37zO6T3AsHMs9TrunVGkIrPu0JFAc=");
            labelRecognition = new LabelRecognition();
            labelRecognition.InitLicense("t0068MgAAAJ96WjrYtvjfkUWwA0Qm5mWoK+Ri+Npyyqvfw2H/dNP+cgj2N2GaZLaJ9w9K7kCra5DghpdXMYMj3t9jFbIDhxg=");
            comboBox1.SelectedIndex = 0;
        }


        private void pictureBox1_Click(object sender, EventArgs e)
        {
            OpenFileDialog dialog = new OpenFileDialog();
            DialogResult dr = dialog.ShowDialog();
            String filename = "";
            if (dr == DialogResult.OK)
            {
                filename = dialog.FileName;
            }
            else
            {
                return;
            }
            Console.WriteLine(filename);
            pictureBox1.Image= PixelFormatFixed(filename);
            pictureBox1.SizeMode = PictureBoxSizeMode.Zoom;
            pictureBox1.Tag = filename;
            
            textBox1.Text = "";

        }

        private void draw(Image img,Point[] points)
        {
            Graphics g = Graphics.FromImage(img);
            Pen p = new Pen(Color.Red,3);
            g.DrawLine(p, points[0], points[1]);
            g.DrawLine(p, points[1], points[2]);
            g.DrawLine(p, points[2], points[3]);
            g.DrawLine(p, points[3], points[0]);
            g.Save();
            pictureBox1.Image = img;
        }

        private Image PixelFormatFixed(String filename)
        {
            // The original bitmap with the wrong pixel format. 
            // You can check the pixel format with originalBmp.PixelFormat
            Image img = Image.FromFile(filename);
            Bitmap originalBmp = (Bitmap)img;
            // Create a blank bitmap with the same dimensions
            Bitmap tempBitmap = new Bitmap(originalBmp.Width, originalBmp.Height);
            // From this bitmap, the graphics can be obtained, because it has the right PixelFormat
            using (Graphics g = Graphics.FromImage(tempBitmap))
            {
                // Draw the original bitmap onto the graphics of the new bitmap
                g.DrawImage(originalBmp, 0, 0);
            }
            // Use tempBitmap as you would have used originalBmp
            return tempBitmap;
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {

        }

        private void ReadBarcodesButton_Click(object sender, EventArgs e)
        {
            Console.WriteLine("reading dbr");
            if (pictureBox1.Tag == null)
            {
                return;
            }
            if (DBRTemplateTextBox.Text != "")
            {
                try
                {
                    String o = "";
                    reader.InitRuntimeSettingsWithString(DBRTemplateTextBox.Text, EnumConflictMode.CM_OVERWRITE, errorMessage: out o);
                    PublicRuntimeSettings rs = reader.GetRuntimeSettings();
                    rs.ResultCoordinateType = EnumResultCoordinateType.RCT_PIXEL;
                    reader.UpdateRuntimeSettings(rs);
                }
                catch (Exception)
                {

                    throw;
                }
            }
            try
            {
                TextResult[] results = reader.DecodeFile(pictureBox1.Tag.ToString(),"");
                string outputInfo = "Total barcodes found: " + results.Length.ToString();
                Console.WriteLine(outputInfo);
                for (int iIndex = 0; iIndex < results.Length; ++iIndex)
                {
                    int iBarcodeIndex = iIndex + 1;
                    string builder = "Barcode " + iBarcodeIndex.ToString() + ":\r\n";
                    TextResult result = results[iIndex];
                    if (result.BarcodeFormat != 0)
                    {
                        builder += "    Type: " + result.BarcodeFormatString + "\r\n";
                    }
                    else
                    {
                        builder += "    Type: " + result.BarcodeFormatString_2 + "\r\n";
                    }
                    builder += "    Value: " + result.BarcodeText + "\r\n";
                    draw(pictureBox1.Image,result.LocalizationResult.ResultPoints);
                    Console.WriteLine(builder);
                    textBox1.Text = textBox1.Text + builder;
                    
                }
                textBox1.Tag = results;

            }
            catch (BarcodeReaderException exp)
            {
                Console.WriteLine(exp.Message);
                textBox1.Text = exp.Message;
            }
        }

        private void ReadLabelsButton_Click(object sender, EventArgs e)
        {
            if (pictureBox1.Tag == null)
            {
                return;
            }

            Console.WriteLine("reading");
            try
            {
                labelRecognition.ResetRuntimeSettings();
                String templateName = LoadDLRTemplate();
                if (templateName == "")
                {
                    DLR_RuntimeSettings rs = labelRecognition.GetRuntimeSettings();
                    //rs.ReferenceRegion.LocalizationSourceType = EnumDLRLocalizationSourceType.DLR_LST_PREDETECTED_REGION;
                    //rs.RegionPredetectionModes.Append(EnumDLRRegionPredetectionMode.DLR_RPM_GENERAL_GRAY_CONTRAST);
                    rs.RegionPredetectionModes.Append(EnumDLRRegionPredetectionMode.DLR_RPM_GENERAL_RGB_CONTRAST);
                    //int index = rs.RegionPredetectionModes.Length;
                    //Console.WriteLine("modes length");
                    //Console.WriteLine(rs.RegionPredetectionModes.Length);
                    //Console.WriteLine(rs.RegionPredetectionModes.GetValue(0));
                    //rs.RegionPredetectionModes.Prepend(EnumDLRRegionPredetectionMode.DLR_RPM_GENERAL_HSV_CONTRAST);
                    //Console.WriteLine(rs.RegionPredetectionModes.Length);
                    //Console.WriteLine(rs.RegionPredetectionModes.GetValue(0));
                    rs.LinesCount = 100;
                    rs.CharacterModelName = comboBox1.SelectedItem.ToString();
                    labelRecognition.UpdateRuntimeSettings(rs);
                    //labelRecognition.SetModeArgument("RegionPredetectionModes", index, "ForeAndBackgroundColours", "[43,60,5]");
                }
               
                DLR_Result[] results = labelRecognition.RecognizeByFile(pictureBox1.Tag.ToString(), templateName);
                OutputDLRResult(results);
            }
            catch (DLR_Exception exp)
            {
                Console.WriteLine(exp);
            }
            catch (Exception exp)
            {
                Console.WriteLine(exp);
            }
        }

        private void ReadLabelsWithDBRButton_Click(object sender, EventArgs e)
        {
            if (pictureBox1.Tag == null)
            {
                return;
            }
            if (textBox1.Tag == null)
            {
                MessageBox.Show("Please read barcodes first.");

                return;
            }
            TextResult[] dbr_result = (TextResult[])textBox1.Tag;
            String templateName = LoadDLRTemplate();
            if (templateName == "")
            {
                DLR_RuntimeSettings rs = labelRecognition.GetRuntimeSettings();
                rs.ReferenceRegion.LocalizationSourceType = EnumDLRLocalizationSourceType.DLR_LST_BARCODE;
                Console.WriteLine(rs.ReferenceRegion.Points.ToString());
                rs.LinesCount = 100;
                rs.CharacterModelName = comboBox1.SelectedItem.ToString();
                labelRecognition.UpdateRuntimeSettings(rs);                
            }

            labelRecognition.UpdateReferenceRegionFromBarcodeResults(dbr_result, templateName);
            DLR_Result[] results = labelRecognition.RecognizeByFile(pictureBox1.Tag.ToString(), templateName);
            
            //labelRecognition.OutputSettingsToFile("D:\\out.json", "");
            OutputDLRResult(results);
        }

        private void OutputDLRResult(DLR_Result[] results)
        {
            string builder = "";
            Console.WriteLine(results.Length);
            for (int i = 0; i < results.Length; ++i)
            {
                
                builder += "Result " + i.ToString() + ": \r\n";
                for (int j = 0; j < results[i].LineResults.Length; ++j)
                {
                    builder += ">>LineResult " + j.ToString() + ": " + results[i].LineResults[j].Text + "\r\n";

                    draw(pictureBox1.Image, results[i].LineResults[j].Location.Points);
                }
            }
            textBox1.Text += builder;
        }

        private String LoadDLRTemplate()
        {
            labelRecognition.ClearAppendedSettings();
            String templateName = "";
            if (DLRTemplateTextBox.Text != "")
            {
                try
                {
                    labelRecognition.AppendSettingsFromString(DLRTemplateTextBox.Text);

                }
                catch (Exception)
                {
                    MessageBox.Show("Wrong json.");
                    throw;
                }

                JObject jo = (JObject)JsonConvert.DeserializeObject(DLRTemplateTextBox.Text);
                List<JToken> array = jo["LabelRecognitionParameterArray"].ToList<JToken>();
                templateName = array[0]["Name"].ToString();
                Console.WriteLine("added template: " + templateName);
                return templateName;
            }
            return "";
        }

        private void ClearButton_Click(object sender, EventArgs e)
        {
            try
            {
                pictureBox1.Image = PixelFormatFixed(pictureBox1.Tag.ToString());
                textBox1.Text = "";
            }
            catch (Exception)
            {

                throw;
            }
            
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }


    }
}
