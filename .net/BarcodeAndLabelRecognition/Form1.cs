using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Dynamsoft;
using Dynamsoft.DBR;
using Dynamsoft.DLR;

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
            pictureBox1.Image= Image.FromFile(filename);
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
                DLR_RuntimeSettings rs = labelRecognition.GetRuntimeSettings();
                rs.ReferenceRegion.LocalizationSourceType = EnumDLRLocalizationSourceType.DLR_LST_PREDETECTED_REGION;
                rs.RegionPredetectionModes.Append(EnumDLRRegionPredetectionMode.DLR_RPM_GENERAL_GRAY_CONTRAST);
                rs.RegionPredetectionModes.Append(EnumDLRRegionPredetectionMode.DLR_RPM_GENERAL_HSV_CONTRAST);
                rs.LinesCount = 100;
                rs.CharacterModelName = comboBox1.SelectedItem.ToString();
                labelRecognition.UpdateRuntimeSettings(rs);
                DLR_Result[] results = labelRecognition.RecognizeByFile(pictureBox1.Tag.ToString(), "");
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
            //labelRecognition.AppendSettingsFromString("{\"LabelRecognitionParameter\":{\"Name\":\"P1\", \"RegionPredetectionModes\":[{\"Mode\":\"DLR_RPM_GENERAL_HSV_CONTRAST\"}], \"ReferenceRegionNameArray\": [\"R1\"]},\"ReferenceRegion\":{\"Name\":\"R1\",\"Localization\":{\"SourceType\":\"DLR_LST_BARCODE\"},\"TextAreaNameArray\":[\"T1\"]},\"TextArea\":{\"Name\":\"T1\",\"CharacterModelName\":\"Number\"}}");
            //labelRecognition.UpdateReferenceRegionFromBarcodeResults(dbr_result, "P1");
            DLR_RuntimeSettings rs = labelRecognition.GetRuntimeSettings();
            rs.ReferenceRegion.LocalizationSourceType = EnumDLRLocalizationSourceType.DLR_LST_BARCODE;        
            rs.CharacterModelName = comboBox1.SelectedItem.ToString();
            labelRecognition.UpdateRuntimeSettings(rs);
            labelRecognition.UpdateReferenceRegionFromBarcodeResults(dbr_result,"");
            
            DLR_Result[] results = labelRecognition.RecognizeByFile(pictureBox1.Tag.ToString(), "");
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


        private void ClearButton_Click(object sender, EventArgs e)
        {
            try
            {
                pictureBox1.Image = Image.FromFile(pictureBox1.Tag.ToString());
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
