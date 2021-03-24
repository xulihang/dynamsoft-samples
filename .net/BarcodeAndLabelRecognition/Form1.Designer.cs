
using System;
using System.ComponentModel;

namespace BarcodeAndLabelRecognition
{
    partial class Form1
    {
        /// <summary>
        /// 必需的设计器变量。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 清理所有正在使用的资源。
        /// </summary>
        /// <param name="disposing">如果应释放托管资源，为 true；否则为 false。</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows 窗体设计器生成的代码

        /// <summary>
        /// 设计器支持所需的方法 - 不要修改
        /// 使用代码编辑器修改此方法的内容。
        /// </summary>
        private void InitializeComponent()
        {
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.ReadBarcodesButton = new System.Windows.Forms.Button();
            this.ReadLabelsButton = new System.Windows.Forms.Button();
            this.ReadLabelsWithDBRButton = new System.Windows.Forms.Button();
            this.backgroundWorker1 = new System.ComponentModel.BackgroundWorker();
            this.ClearButton = new System.Windows.Forms.Button();
            this.DBRTemplateTextBox = new System.Windows.Forms.TextBox();
            this.comboBox1 = new System.Windows.Forms.ComboBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            this.SuspendLayout();
            // 
            // pictureBox1
            // 
            this.pictureBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.pictureBox1.Location = new System.Drawing.Point(26, 28);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(301, 425);
            this.pictureBox1.TabIndex = 0;
            this.pictureBox1.TabStop = false;
            this.pictureBox1.Click += new System.EventHandler(this.pictureBox1_Click);
            // 
            // textBox1
            // 
            this.textBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.textBox1.Location = new System.Drawing.Point(350, 28);
            this.textBox1.Multiline = true;
            this.textBox1.Name = "textBox1";
            this.textBox1.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox1.Size = new System.Drawing.Size(315, 195);
            this.textBox1.TabIndex = 1;
            this.textBox1.TextChanged += new System.EventHandler(this.textBox1_TextChanged);
            // 
            // ReadBarcodesButton
            // 
            this.ReadBarcodesButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.ReadBarcodesButton.Location = new System.Drawing.Point(714, 28);
            this.ReadBarcodesButton.Name = "ReadBarcodesButton";
            this.ReadBarcodesButton.RightToLeft = System.Windows.Forms.RightToLeft.No;
            this.ReadBarcodesButton.Size = new System.Drawing.Size(89, 23);
            this.ReadBarcodesButton.TabIndex = 3;
            this.ReadBarcodesButton.Text = "Read Barcodes";
            this.ReadBarcodesButton.UseVisualStyleBackColor = true;
            this.ReadBarcodesButton.Click += new System.EventHandler(this.ReadBarcodesButton_Click);
            // 
            // ReadLabelsButton
            // 
            this.ReadLabelsButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.ReadLabelsButton.Location = new System.Drawing.Point(714, 75);
            this.ReadLabelsButton.Name = "ReadLabelsButton";
            this.ReadLabelsButton.Size = new System.Drawing.Size(89, 23);
            this.ReadLabelsButton.TabIndex = 4;
            this.ReadLabelsButton.Text = "Read Labels";
            this.ReadLabelsButton.UseVisualStyleBackColor = true;
            this.ReadLabelsButton.Click += new System.EventHandler(this.ReadLabelsButton_Click);
            // 
            // ReadLabelsWithDBRButton
            // 
            this.ReadLabelsWithDBRButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.ReadLabelsWithDBRButton.Location = new System.Drawing.Point(714, 122);
            this.ReadLabelsWithDBRButton.Name = "ReadLabelsWithDBRButton";
            this.ReadLabelsWithDBRButton.Size = new System.Drawing.Size(89, 53);
            this.ReadLabelsWithDBRButton.TabIndex = 5;
            this.ReadLabelsWithDBRButton.Text = "Read Labels with the help of DBR results";
            this.ReadLabelsWithDBRButton.UseVisualStyleBackColor = true;
            this.ReadLabelsWithDBRButton.Click += new System.EventHandler(this.ReadLabelsWithDBRButton_Click);
            // 
            // backgroundWorker1
            // 
            this.backgroundWorker1.DoWork += new System.ComponentModel.DoWorkEventHandler(this.backgroundWorker1_DoWork);
            // 
            // ClearButton
            // 
            this.ClearButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.ClearButton.Location = new System.Drawing.Point(714, 200);
            this.ClearButton.Name = "ClearButton";
            this.ClearButton.Size = new System.Drawing.Size(89, 23);
            this.ClearButton.TabIndex = 6;
            this.ClearButton.Text = "Clear";
            this.ClearButton.UseVisualStyleBackColor = true;
            this.ClearButton.Click += new System.EventHandler(this.ClearButton_Click);
            // 
            // DBRTemplateTextBox
            // 
            this.DBRTemplateTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.DBRTemplateTextBox.Location = new System.Drawing.Point(350, 255);
            this.DBRTemplateTextBox.Multiline = true;
            this.DBRTemplateTextBox.Name = "DBRTemplateTextBox";
            this.DBRTemplateTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.DBRTemplateTextBox.Size = new System.Drawing.Size(315, 198);
            this.DBRTemplateTextBox.TabIndex = 7;
            // 
            // comboBox1
            // 
            this.comboBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.comboBox1.FormattingEnabled = true;
            this.comboBox1.Items.AddRange(new object[] {
            "Number",
            "Letter",
            "NumberLetter",
            "NumberUppercase"});
            this.comboBox1.Location = new System.Drawing.Point(714, 275);
            this.comboBox1.Name = "comboBox1";
            this.comboBox1.Size = new System.Drawing.Size(89, 21);
            this.comboBox1.TabIndex = 8;
            // 
            // label1
            // 
            this.label1.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(347, 239);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(80, 13);
            this.label1.TabIndex = 9;
            this.label1.Text = "DBR Template:";
            this.label1.Click += new System.EventHandler(this.label1_Click);
            // 
            // label2
            // 
            this.label2.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(714, 256);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(90, 13);
            this.label2.TabIndex = 10;
            this.label2.Text = "DLR OCR Model:";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(843, 478);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.comboBox1);
            this.Controls.Add(this.DBRTemplateTextBox);
            this.Controls.Add(this.ClearButton);
            this.Controls.Add(this.ReadLabelsWithDBRButton);
            this.Controls.Add(this.ReadLabelsButton);
            this.Controls.Add(this.ReadBarcodesButton);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.pictureBox1);
            this.Name = "Form1";
            this.Text = "Form1";
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        private void backgroundWorker1_DoWork(object sender, DoWorkEventArgs e)
        {
            throw new NotImplementedException();
        }

        #endregion

        private System.Windows.Forms.PictureBox pictureBox1;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button ReadBarcodesButton;
        private System.Windows.Forms.Button ReadLabelsButton;
        private System.Windows.Forms.Button ReadLabelsWithDBRButton;
        private System.ComponentModel.BackgroundWorker backgroundWorker1;
        private System.Windows.Forms.Button ClearButton;
        private System.Windows.Forms.TextBox DBRTemplateTextBox;
        private System.Windows.Forms.ComboBox comboBox1;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
    }
}

